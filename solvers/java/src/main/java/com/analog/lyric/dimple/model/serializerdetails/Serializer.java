/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.serializerdetails;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.xmlSerializer;


public class Serializer
{
	private boolean _dbg;
	
	public Serializer(boolean dbg)
	{
		_dbg = dbg;
	}
	
	private void addVariableElement(VariableBase v, int boundaryIdx, Element Parent, Document Doc) 
	{
		Element e = Doc.createElement("node");
		e.setAttribute("type", "Variable");
		e.setAttribute("id", v.getUUID().toString());
		String explicitName = "";
		if(v.getExplicitName() != null)
		{
			explicitName = v.getExplicitName();
		}
		e.setAttribute("explicitName", explicitName);
		String class_string = v.getModelerClassName();
		String belief_string = "";
		Object belief = v.getBeliefObject();
		for(Object o : ((double[])belief))
		{
			belief_string += o.toString();
			belief_string += " ";
		}
		Domain domain = v.getDomain();
		
		appendDomainElement(domain, Doc, e);

		/*
		//TDOO: override toString
		String domain_string = domain.toString();
		/*
		for(Object o : domain)
		{
			domain_string += o.toString();
			domain_string += " ";
		}
		*/
		
		
		String input_string = "";
		Object input = v.getInputObject();

		for(Object o : ((double[])input))
		{
			input_string += o.toString();
			input_string += " ";
		}
			
		e.setAttribute("class", class_string);
		e.setAttribute("belief", belief_string);
		//e.setAttribute("domain", domain_string);
		e.setAttribute("input", input_string);
		if(boundaryIdx != -1)
		{
			e.setAttribute("boundaryIdx", Integer.toString(boundaryIdx));
		}
		
		Parent.appendChild(e);
	}
	private void addFunctionElement(Factor f, 
									Element Parent, 
									Document Doc, 
									HashMap<Integer,
											xmlsFactorTable> factorTables) 	
	{
        Element e = Doc.createElement("node");
		e.setAttribute("type", "Function");		
		e.setAttribute("id", f.getUUID().toString());
		String explicitName = "";
		if(f.getExplicitName() != null)
		{
			explicitName = f.getExplicitName();
		}
		e.setAttribute("explicitName", explicitName);
		e.setAttribute("class", f.getModelerFunctionName());
			
		ArrayList<INode> ports = f.getSiblings();
		e.setAttribute("numEdges", Integer.toString(ports.size()));			

		// * TODO TableFactor?
		if(f.isDiscrete())
		{
			DiscreteFactor tf = (DiscreteFactor) f;
			FactorTable ct = tf.getFactorTable();
			int hash = ct.hashCode();
			if(!factorTables.containsKey(hash))
			{
				xmlsFactorTable xct = new xmlsFactorTable(f.getModelerFunctionName(),
													hash,
													ct.getIndices(),
													ct.getWeights(),
													ct.getDomains());
				factorTables.put(hash, xct);
			}
			e.setAttribute("comboTable", Integer.toString(hash));
		}	

		
		Parent.appendChild(e);
					
		for (int i = 0; i < ports.size(); i++)
		{			
			Element edgeElement = Doc.createElement("edge");
			VariableBase targetV = (VariableBase)(f.getConnectedNodeFlat(i));

			edgeElement.setAttribute("source", f.getUUID().toString());
			edgeElement.setAttribute("target", targetV.getUUID().toString());
			edgeElement.setAttribute("srcIdx", Integer.toString(i));
			
			Parent.appendChild(edgeElement);		
		}	        	
	}
	
	private void appendDomainElement(Domain domain, Document doc, Element parent) 
	{
		if (!domain.isDiscrete())
			throw new DimpleException("only support discrete domains for now");
		
		DiscreteDomain dd = (DiscreteDomain)domain;
		
		Element domain_entry = doc.createElement("domain");
		
		for (int j = 0; j < dd.size(); j++)
		{
			Element domain_item = doc.createElement("domain_element");
			Object domainElementObject = dd.getElements()[j]; 
			String domainElementString = "null";
			if(domainElementObject != null)
			{
				domainElementString = domainElementObject.toString().toString();
			}
			domain_item.setAttribute("value", domainElementString);
			domain_entry.appendChild(domain_item);
		}
		parent.appendChild(domain_entry);

	}
	
	private void appendFactorTableElement(xmlsFactorTable xct,
			 Document doc,
			 Element parent) 
	{
		Element elFactorTable = doc.createElement("ComboTable");
		elFactorTable.setAttribute("id", Integer.toString(xct._ephemeralId));
		elFactorTable.setAttribute("function", xct._functionName);
		Element elFactorTable_size = doc.createElement("size");
		elFactorTable_size.setAttribute("rows", String.format("%d", xct._indices.length));
		elFactorTable_size.setAttribute("columns", String.format("%d", xct._indices[0].length));
		elFactorTable.appendChild(elFactorTable_size);
		
		Element elFactorTable_indices = doc.createElement("indices");
		for(int row = 0; row <  xct._indices.length; ++row)
		{
			for(int column = 0; column < xct._indices[row].length; ++column)
			{
				Element elEntry = doc.createElement("entry");
				elEntry.setAttribute("pretty", String.format("r:c:v [%d][%d][%d]", row, column, xct._indices[row][column]));
				elEntry.setAttribute("row", String.format("%d", row));
				elEntry.setAttribute("column", String.format("%d", column));
				elEntry.setAttribute("value", String.format("%d", xct._indices[row][column]));
				elFactorTable_indices.appendChild(elEntry);
			}
		}
		elFactorTable.appendChild(elFactorTable_indices);
		
		Element elValue = doc.createElement("values");
		for(int row = 0; row <  xct._values.length; ++row)
		{
			Element value_entry = doc.createElement("value");
			value_entry.setAttribute("row", String.format("%d", row));
			value_entry.setAttribute("value", String.format("%f", xct._values[row]));
			elValue.appendChild(value_entry);
		}
		elFactorTable.appendChild(elValue);
		
		Element elDomains = doc.createElement("domains");
		for (int i = 0; i < xct._domains.length; i++)
		{
			appendDomainElement(xct._domains[i], doc, elDomains);
		}
		elFactorTable.appendChild(elDomains);


		
		parent.appendChild(elFactorTable);		
	}
	
	
	
	public String serializeToXML(FactorGraph fg, String FileName, String targetDirectory)
	{
		trace(String.format("++serializeToXML [%s] [%s]", FileName, targetDirectory));
		String SerializedFileName = targetDirectory;
		try {			
	        FactorList fl = fg.getNonGraphFactorsFlat();
	        com.analog.lyric.dimple.model.VariableList vl = fg.getVariablesFlat();
	        com.analog.lyric.dimple.model.VariableList bl = fg.getBoundaryVariables();
	        trace(String.format("%d Factors", fl.size()));
	        trace(String.format("%d Variables", vl.size()));
	        trace(String.format("%d Boundary Variables", bl.size()));

			DocumentBuilderFactory factory 
	         = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        DOMImplementation impl = builder.getDOMImplementation();

	        
	        // Create the document
	        Document doc = impl.createDocument(null, 
	        								   FileName, 
	        								   null);
       	    Element root = doc.getDocumentElement();
	        Element Version = doc.createElement("version");
	        Version.setAttribute("version", xmlSerializer.VERSION);
	        root.appendChild(Version);
	       
	        Element graph = doc.createElement("graph");
	        graph.setAttribute("id", fg.getUUID().toString());
	        String explicitName = "";
	        if(fg.getExplicitName() != null)
	        {
	        	explicitName = fg.getExplicitName();
	        }
	        graph.setAttribute("explicitName", explicitName);
	        graph.setAttribute("edgedefault", "undirected");
	        graph.setAttribute("numBoundaryVariables", 
	        				   Integer.toString(bl.size()));			
	        graph.setAttribute("solverClass", fg.getFactorGraphFactory().getClass().getName());
	        //Variables
	        int i = 0;
	        for(VariableBase v : bl)
	        {
        		addVariableElement((VariableBase)v, i, graph, doc);		        		
        		i++;
	        }
	        for(VariableBase v : vl)
	        {
	        	if(!bl.contains(v))
	        	{
	        		addVariableElement((VariableBase)v, -1, graph, doc);		        		
	        	}
	        }
	        
	        //Functions and connectivity
	        HashMap<Integer, xmlsFactorTable> comboTableMap = new HashMap<Integer, xmlsFactorTable>();	        
	        for(Factor f : fl)
	        {
	        	addFunctionElement(f, graph, doc, comboTableMap);
	        }
	        
	        //combo tables
	        Element factorTables = doc.createElement("comboTables");
	        for(xmlsFactorTable xct : comboTableMap.values())
	        {
	        	appendFactorTableElement(xct,
										doc,
										factorTables);
	        
	        }
	        graph.appendChild(factorTables);
	        
	        root.appendChild(graph);
	        		        
	        String out = com.analog.lyric.dimple.model.xmlSerializer.prettyFormat(doc);
	        
	        SerializedFileName += "/";
	        SerializedFileName += FileName;
	        SerializedFileName += ".xml";
	        	
	        try 
	        { 
		        BufferedWriter outFile = new BufferedWriter(new FileWriter(SerializedFileName)); 
		        outFile.write(out); 
		        outFile.close(); 
	        } 
	        catch (IOException e) 
	        {
	        	System.out.println("IO Exception: " + e.getMessage());
	        	SerializedFileName = "";
	        } 
	 
	      }
	      catch (FactoryConfigurationError e) 
	      { 
	        System.out.println("Could not locate a JAXP factory class"); 
        	SerializedFileName = "";
	      }
	      catch (ParserConfigurationException e) 
	      { 
	        System.out.println(
	          "Could not locate a JAXP DocumentBuilder class"
	        ); 
        	SerializedFileName = "";
	      }
	      catch (DOMException e) 
	      {
	        System.err.println("DOMException: " + e); 
        	SerializedFileName = "";
	        
	      }
	      catch(Exception e)
	      {
	        	System.out.println("Exception: " + e.getMessage());	    	  
	        	SerializedFileName = "";
	      }
	      
	      trace(String.format("--serializeToXML [%s]", SerializedFileName));
	      return SerializedFileName;
	}
	public String serializeFactorTableToXML(int[][] indices, double[] values, DiscreteDomain [] domains,String ctName, String targetDirectory)
	{
		trace(String.format("++serializeComboTableToXML [%s] [%s]", ctName, targetDirectory));
		String SerializedFileName = targetDirectory;
		try {			

			DocumentBuilderFactory factory 
	         = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        DOMImplementation impl = builder.getDOMImplementation();

	        
	        // Create the document
	        Document doc = impl.createDocument(null, 
	        								   ctName, 
	        								   null);
       	    Element root = doc.getDocumentElement();
	        Element version = doc.createElement("version");
	        version.setAttribute("version", xmlSerializer.VERSION);
	        root.appendChild(version);
	       
	        xmlsFactorTable xct = new xmlsFactorTable(ctName, 1, indices, values,domains);
	        
	    	appendFactorTableElement(xct, 
	    							doc,
									root);

	        String out = com.analog.lyric.dimple.model.xmlSerializer.prettyFormat(doc);
	        
	        SerializedFileName += "/";
	        SerializedFileName += ctName;
	        SerializedFileName += ".xml";
	        	
	        try 
	        { 
		        BufferedWriter outFile = new BufferedWriter(new FileWriter(SerializedFileName)); 
		        outFile.write(out); 
		        outFile.close(); 
	        } 
	        catch (IOException e) 
	        {
	        	System.out.println("IO Exception: " + e.getMessage());
	        	SerializedFileName = "";
	        } 
	 
	      }
	      catch (FactoryConfigurationError e) 
	      { 
	        System.out.println("Could not locate a JAXP factory class"); 
        	SerializedFileName = "";
	      }
	      catch (ParserConfigurationException e) 
	      { 
	        System.out.println(
	          "Could not locate a JAXP DocumentBuilder class"
	        ); 
        	SerializedFileName = "";
	      }
	      catch (DOMException e) 
	      {
	        System.err.println("DOMException: " + e); 
        	SerializedFileName = "";
	        
	      }
	      catch(Exception e)
	      {
	        	System.out.println("Exception: " + e.getMessage() + " " + e.toString());	    	  
	        	SerializedFileName = "";
	      }
	      
	      trace(String.format("--serializeComboTableToXML [%s]", SerializedFileName));
	      return SerializedFileName;		
	}	
	public void setDbg(boolean dbg) 
	{
		_dbg = dbg;
	}
	public void trace(String format, Object... args)
	{
		if (_dbg == true)
		{
			System.out.println(String.format(format, args));
		}
	}
}
