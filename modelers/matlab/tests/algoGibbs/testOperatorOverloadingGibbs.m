%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function testOperatorOverloadingGibbs()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testOperatorOverloadingGibbs');

test1(debugPrint, repeatable);
test2(debugPrint, repeatable);
test3(debugPrint, repeatable);
test4(debugPrint, repeatable);
test5(debugPrint, repeatable);

dtrace(debugPrint, '--testOperatorOverloadingGibbs');

end


function test1(debugPrint, repeatable)

numSamples = 1000;
scansPerSample = 50;
burnInScans = 10;

graph1 = FactorGraph();
graph1.Solver = 'Gibbs';
graph1.Solver.setNumSamples(numSamples);
graph1.Solver.setScansPerSample(scansPerSample);
graph1.Solver.setBurnInScans(burnInScans);


a1 = Real(FactorFunction('Normal',3,1/12^2));       % Use FactorFunction
b1 = Real({'Normal',7,1/17^2});                     % Use cell notation

z1 = a1 + b1;

if (repeatable)
    graph1.Solver.setSeed(1);					% Make this repeatable
end

graph1.Solver.saveAllSamples();
graph1.solve();


graph2 = FactorGraph();
graph2.Solver = 'Gibbs';
graph2.Solver.setNumSamples(numSamples);
graph2.Solver.setScansPerSample(scansPerSample);
graph2.Solver.setBurnInScans(burnInScans);

a2 = Real(FactorFunction('Normal',3,1/12^2));       % Use FactorFunction
b2 = Real({'Normal',7,1/17^2});                     % Use cell notation
z2 = Real();
graph2.addFactor('Sum',z2,a2,b2);

if (repeatable)
    graph2.Solver.setSeed(1);					% Make this repeatable
end
graph2.Solver.saveAllSamples();
graph2.solve();

z1Samples = z1.Solver.getAllSamples();
z2Samples = z2.Solver.getAllSamples();

assertElementsAlmostEqual(z1Samples, z2Samples);

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
assertElementsAlmostEqual(a1Samples + b1Samples, z1Samples);

end




function test2(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

w1 = Real(FactorFunction('Gamma',1,1));
x1 = Real([-pi pi]);
y1 = Real({'Normal',0,1/10^2});
z1 = Real([-.99 .99]);

a1 = abs(z1);
b1 = exp(y1);
c1 = log(w1);
d1 = sqrt(w1);
e1 = sin(x1);
f1 = cos(x1);
g1 = tan(x1);
h1 = asin(z1);
i1 = acos(z1);
j1 = atan(z1);
k1 = sinh(y1);
l1 = cosh(y1);
m1 = tanh(y1);
n1 = a1 + b1 + c1 + d1 + e1 + f1 + g1 + h1 + i1 + j1 + k1 + l1 + m1;


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

a1Samples = a1.Solver.getAllSamples();
b1Samples = b1.Solver.getAllSamples();
c1Samples = c1.Solver.getAllSamples();
d1Samples = d1.Solver.getAllSamples();
e1Samples = e1.Solver.getAllSamples();
f1Samples = f1.Solver.getAllSamples();
g1Samples = g1.Solver.getAllSamples();
h1Samples = h1.Solver.getAllSamples();
i1Samples = i1.Solver.getAllSamples();
j1Samples = j1.Solver.getAllSamples();
k1Samples = k1.Solver.getAllSamples();
l1Samples = l1.Solver.getAllSamples();
m1Samples = m1.Solver.getAllSamples();
n1Samples = n1.Solver.getAllSamples();

w1Samples = w1.Solver.getAllSamples();
x1Samples = x1.Solver.getAllSamples();
y1Samples = y1.Solver.getAllSamples();
z1Samples = z1.Solver.getAllSamples();


assertElementsAlmostEqual(a1Samples, abs(z1Samples), 'absolute');
assertElementsAlmostEqual(b1Samples, exp(y1Samples), 'absolute');
assertElementsAlmostEqual(c1Samples, log(w1Samples), 'absolute');
assertElementsAlmostEqual(d1Samples, sqrt(w1Samples), 'absolute');
assertElementsAlmostEqual(e1Samples, sin(x1Samples), 'absolute');
assertElementsAlmostEqual(f1Samples, cos(x1Samples), 'absolute');
assertElementsAlmostEqual(g1Samples, tan(x1Samples), 'absolute');
assertElementsAlmostEqual(h1Samples, asin(z1Samples), 'absolute');
assertElementsAlmostEqual(i1Samples, acos(z1Samples), 'absolute');
assertElementsAlmostEqual(j1Samples, atan(z1Samples), 'absolute');
assertElementsAlmostEqual(k1Samples, sinh(y1Samples), 'absolute');
assertElementsAlmostEqual(l1Samples, cosh(y1Samples), 'absolute');
assertElementsAlmostEqual(m1Samples, tanh(y1Samples), 'absolute');
assertElementsAlmostEqual(n1Samples, a1Samples + b1Samples + c1Samples + d1Samples + e1Samples + f1Samples + g1Samples + h1Samples + i1Samples + j1Samples + k1Samples + l1Samples + m1Samples, 'absolute');


end




function test3(debugPrint, repeatable)

numSamples = 20;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

N = 4;
a = Discrete(1:5);
b = Discrete(1:10, 1, N);
c = Real([0 1]);
d = Real([0 1], 1, N);
M = 3;
bt = Discrete(1:10, N, 1);
dt = Real([0 1], N, 1);
A = Discrete(1:5, M, N);
B = Real([0 1], M, N);
a.setNames('a');
b.setNames('b');
c.setNames('c');
d.setNames('d');

e = b + 1;              % Discrete vector and scalar constant
f = a * 3;              % Discrete scalar and scalar constant
g = b - a;              % Discrete vector and scalar

h = 1 + b;              % Discrete vector and scalar constant
i = 3 * a;              % Discrete scalar and scalar constant
j = a + -b;             % Discrete vector and scalar

k = d + 1;              % Real vector and scalar constant
l = c * 3;              % Real scalar and scalar constant
m = d - c;              % Real vector and scalar

n = 1 + d;              % Real vector and scalar constant
o = 3 * c;              % Real scalar and scalar constant
p = c + -d;             % Real vector and scalar

q = a + c;              % Discrete scalar and Real scalar
r = b .* c;             % Discrete vector and Real scalar
s = a + d;              % Discrete scalar and Real vector
t = b .* d;             % Discrete vector and Real vector

u = c + a;              % Discrete scalar and Real scalar
v = c .* b;             % Discrete vector and Real scalar
w = d + a;              % Discrete scalar and Real vector
x = d .* b;             % Discrete vector and Real vector

y = a * b;              % Discrete scalar times vector (non-pointwise operator)
z = d * c;              % Real scalar times vector (non-pointwise operator)
aa = b^a;               % Discrete vector to scalar power (non-pointwise operator)
bb = d^c;               % Real vector to scalar power (non-pointwise operator)
cc = c.^d;              % Real scalar to vector power (pointwise operator)

dd = A * bt;            % Discrete matrix times discrete column vector
ee = A * dt;            % Discrete matrix times real column vector
ff = B * bt;            % Real matrix times discrete column vector
gg = B * dt;            % Real matrix times real column vector

hh = b * A';            % Discrete row vector times discrete matrix
ii = d * A';            % Real row vector times discrete matrix
jj = b * B';            % Discrete row vector times real matrix
kk = d * B';            % Real row vector times real matrix

CA = [1 2 3 4; 5 6 7 8; 9 10 11 12];
cb = [9 8 7 6];
cbt = cb';
ll = CA * bt;           % Constant matrix times discrete vector
mm = CA * dt;           % Constant matrix times real vector
nn = A * cbt;           % Discrete variable matrix times constant vector
oo = B * cbt;           % Real variable matrix times constant vector
pp = b * CA';           % Discrete variable vector times constant matrix
qq = d * CA';           % Real variable vector times constant matrix
rr = cb * A';           % Constant vector times discrete matrix
tt = cb * B';           % Constant vector times real matrix

uu = a / 3;             % Discrete scalar and scalar constant
vv = 3 / a;             % Discrete scalar and scalar constant
ww = c / 3;             % Real scalar and scalar constant
xx = 3 / c;             % Real scalar and scalar constant
yy = b ./ c;            % Discrete vector and Real scalar
zz = b ./ d;            % Discrete vector and Real vector
aaa = c ./ b;           % Discrete vector and Real scalar
bbb = d ./ b;           % Discrete vector and Real vector
ccc = a ./ b;           % Discrete scalar and discrete vector
ddd = d ./ c;           % Real scalar and real vector


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.invokeSolverMethodWithReturnValue('getAllSamples');
bs = b.invokeSolverMethodWithReturnValue('getAllSamples');
cs = c.invokeSolverMethodWithReturnValue('getAllSamples');
ds = d.invokeSolverMethodWithReturnValue('getAllSamples');
es = e.invokeSolverMethodWithReturnValue('getAllSamples');
fs = f.invokeSolverMethodWithReturnValue('getAllSamples');
gs = g.invokeSolverMethodWithReturnValue('getAllSamples');
hs = h.invokeSolverMethodWithReturnValue('getAllSamples');
is = i.invokeSolverMethodWithReturnValue('getAllSamples');
js = j.invokeSolverMethodWithReturnValue('getAllSamples');
ks = k.invokeSolverMethodWithReturnValue('getAllSamples');
ls = l.invokeSolverMethodWithReturnValue('getAllSamples');
ms = m.invokeSolverMethodWithReturnValue('getAllSamples');
ns = n.invokeSolverMethodWithReturnValue('getAllSamples');
os = o.invokeSolverMethodWithReturnValue('getAllSamples');
ps = p.invokeSolverMethodWithReturnValue('getAllSamples');
qs = q.invokeSolverMethodWithReturnValue('getAllSamples');
rs = r.invokeSolverMethodWithReturnValue('getAllSamples');
ss = s.invokeSolverMethodWithReturnValue('getAllSamples');
ts = t.invokeSolverMethodWithReturnValue('getAllSamples');
us = u.invokeSolverMethodWithReturnValue('getAllSamples');
vs = v.invokeSolverMethodWithReturnValue('getAllSamples');
ws = w.invokeSolverMethodWithReturnValue('getAllSamples');
xs = x.invokeSolverMethodWithReturnValue('getAllSamples');
ys = y.invokeSolverMethodWithReturnValue('getAllSamples');
zs = z.invokeSolverMethodWithReturnValue('getAllSamples');
aas = aa.invokeSolverMethodWithReturnValue('getAllSamples');
bbs = bb.invokeSolverMethodWithReturnValue('getAllSamples');
ccs = cc.invokeSolverMethodWithReturnValue('getAllSamples');
dds = dd.invokeSolverMethodWithReturnValue('getAllSamples');
ees = ee.invokeSolverMethodWithReturnValue('getAllSamples');
ffs = ff.invokeSolverMethodWithReturnValue('getAllSamples');
ggs = gg.invokeSolverMethodWithReturnValue('getAllSamples');
hhs = hh.invokeSolverMethodWithReturnValue('getAllSamples');
iis = ii.invokeSolverMethodWithReturnValue('getAllSamples');
jjs = jj.invokeSolverMethodWithReturnValue('getAllSamples');
kks = kk.invokeSolverMethodWithReturnValue('getAllSamples');
lls = ll.invokeSolverMethodWithReturnValue('getAllSamples');
mms = mm.invokeSolverMethodWithReturnValue('getAllSamples');
nns = nn.invokeSolverMethodWithReturnValue('getAllSamples');
oos = oo.invokeSolverMethodWithReturnValue('getAllSamples');
pps = pp.invokeSolverMethodWithReturnValue('getAllSamples');
qqs = qq.invokeSolverMethodWithReturnValue('getAllSamples');
rrs = rr.invokeSolverMethodWithReturnValue('getAllSamples');
tts = tt.invokeSolverMethodWithReturnValue('getAllSamples');
uus = uu.invokeSolverMethodWithReturnValue('getAllSamples');
vvs = vv.invokeSolverMethodWithReturnValue('getAllSamples');
wws = ww.invokeSolverMethodWithReturnValue('getAllSamples');
xxs = xx.invokeSolverMethodWithReturnValue('getAllSamples');
yys = yy.invokeSolverMethodWithReturnValue('getAllSamples');
zzs = zz.invokeSolverMethodWithReturnValue('getAllSamples');
aaas = aaa.invokeSolverMethodWithReturnValue('getAllSamples');
bbbs = bbb.invokeSolverMethodWithReturnValue('getAllSamples');
cccs = ccc.invokeSolverMethodWithReturnValue('getAllSamples');
ddds = ddd.invokeSolverMethodWithReturnValue('getAllSamples');
bts = bt.invokeSolverMethodWithReturnValue('getAllSamples');
dts = dt.invokeSolverMethodWithReturnValue('getAllSamples');
As = A.invokeSolverMethodWithReturnValue('getAllSamples');
Bs = B.invokeSolverMethodWithReturnValue('getAllSamples');

% Convert to 2D double arrays (numSamples x N)
asx = repmat(arrayfun(@(x)x, as), 1, N);
bsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bs, 'UniformOutput', false));
csx = repmat(arrayfun(@(x)x, cs), 1, N);
dsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ds, 'UniformOutput', false));

esx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), es, 'UniformOutput', false));
fsx = repmat(arrayfun(@(x)x, fs), 1, N);
gsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), gs, 'UniformOutput', false));

hsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hs, 'UniformOutput', false));
isx = repmat(arrayfun(@(x)x, is), 1, N);
jsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), js, 'UniformOutput', false));

ksx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ks, 'UniformOutput', false));
lsx = repmat(arrayfun(@(x)x, ls), 1, N);
msx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ms, 'UniformOutput', false));

nsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ns, 'UniformOutput', false));
osx = repmat(arrayfun(@(x)x, os), 1, N);
psx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ps, 'UniformOutput', false));

qsx = repmat(arrayfun(@(x)x, qs), 1, N);
rsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), rs, 'UniformOutput', false));
ssx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ss, 'UniformOutput', false));
tsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ts, 'UniformOutput', false));

usx = repmat(arrayfun(@(x)x, us), 1, N);
vsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), vs, 'UniformOutput', false));
wsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ws, 'UniformOutput', false));
xsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), xs, 'UniformOutput', false));

ysx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ys, 'UniformOutput', false));
zsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), zs, 'UniformOutput', false));
aasx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aas, 'UniformOutput', false));
bbsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bbs, 'UniformOutput', false));
ccsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ccs, 'UniformOutput', false));

ddsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), dds', 'UniformOutput', false))';
eesx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ees', 'UniformOutput', false))';
ffsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ffs', 'UniformOutput', false))';
ggsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ggs', 'UniformOutput', false))';

hhsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hhs, 'UniformOutput', false));
iisx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), iis, 'UniformOutput', false));
jjsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), jjs, 'UniformOutput', false));
kksx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), kks, 'UniformOutput', false));

llsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), lls', 'UniformOutput', false))';
mmsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), mms', 'UniformOutput', false))';
nnsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), nns', 'UniformOutput', false))';
oosx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), oos', 'UniformOutput', false))';

ppsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), pps, 'UniformOutput', false));
qqsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), qqs, 'UniformOutput', false));
rrsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), rrs, 'UniformOutput', false));
ttsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), tts, 'UniformOutput', false));

uusx = repmat(arrayfun(@(x)x, uus), 1, N);
vvsx = repmat(arrayfun(@(x)x, vvs), 1, N);
wwsx = repmat(arrayfun(@(x)x, wws), 1, N);
xxsx = repmat(arrayfun(@(x)x, xxs), 1, N);

yysx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), yys, 'UniformOutput', false));
zzsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), zzs, 'UniformOutput', false));
aaasx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aaas, 'UniformOutput', false));
bbbsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bbbs, 'UniformOutput', false));
cccsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), cccs, 'UniformOutput', false));
dddsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ddds, 'UniformOutput', false));


btsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), bts', 'UniformOutput', false))';
dtsx = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), dts', 'UniformOutput', false))';
Asx = cellfun(@(x)(arrayfun(@(y)y,x)), As, 'UniformOutput', false);
Bsx = cellfun(@(x)(arrayfun(@(y)y,x)), Bs, 'UniformOutput', false);
Asxx = zeros(M, N, numSamples);
Bsxx = zeros(M, N, numSamples);
for sample = 1:numSamples
    for ri = 1:M
        for ci = 1:N
            Asxx(ri, ci, sample) = Asx{ri, ci}(sample);
            Bsxx(ri, ci, sample) = Bsx{ri, ci}(sample);
        end
    end
end

% Compare results
assertElementsAlmostEqual(esx, bsx + 1, 'absolute');            
assertElementsAlmostEqual(fsx, asx * 3, 'absolute');            
assertElementsAlmostEqual(gsx, bsx - asx, 'absolute');            

assertElementsAlmostEqual(hsx, 1 + bsx, 'absolute');            
assertElementsAlmostEqual(isx, 3 * asx, 'absolute');            
assertElementsAlmostEqual(jsx, asx + -bsx, 'absolute');           

assertElementsAlmostEqual(ksx, dsx + 1, 'absolute');            
assertElementsAlmostEqual(lsx, csx * 3, 'absolute');            
assertElementsAlmostEqual(msx, dsx - csx, 'absolute');            

assertElementsAlmostEqual(nsx, 1 + dsx, 'absolute');            
assertElementsAlmostEqual(osx, 3 * csx, 'absolute');            
assertElementsAlmostEqual(psx, csx + -dsx, 'absolute');           

assertElementsAlmostEqual(qsx, asx + csx, 'absolute');            
assertElementsAlmostEqual(rsx, bsx .* csx, 'absolute');           
assertElementsAlmostEqual(ssx, asx + dsx, 'absolute');            
assertElementsAlmostEqual(tsx, bsx .* dsx, 'absolute');           

assertElementsAlmostEqual(usx, csx + asx, 'absolute');            
assertElementsAlmostEqual(vsx, csx .* bsx, 'absolute');           
assertElementsAlmostEqual(wsx, dsx + asx, 'absolute');           
assertElementsAlmostEqual(xsx, dsx .* bsx, 'absolute');          

assertElementsAlmostEqual(ysx, asx .* bsx, 'absolute');
assertElementsAlmostEqual(zsx, dsx .* csx, 'absolute');
assertElementsAlmostEqual(aasx, bsx .^ asx, 'absolute');
assertElementsAlmostEqual(bbsx, dsx .^ csx, 'absolute');
assertElementsAlmostEqual(ccsx, csx .^ dsx, 'absolute');

assertElementsAlmostEqual(uusx, asx / 3, 'absolute');
assertElementsAlmostEqual(vvsx, 3 ./ asx, 'absolute');
assertElementsAlmostEqual(wwsx, csx / 3, 'absolute');
assertElementsAlmostEqual(xxsx, 3 ./ csx, 'absolute');
assertElementsAlmostEqual(yysx, bsx ./ csx, 'absolute');
assertElementsAlmostEqual(zzsx, bsx ./ dsx, 'absolute');
assertElementsAlmostEqual(aaasx, csx ./ bsx, 'absolute');
assertElementsAlmostEqual(bbbsx, dsx ./ bsx, 'absolute');
assertElementsAlmostEqual(cccsx, asx ./ bsx, 'absolute');
assertElementsAlmostEqual(dddsx, dsx ./ csx, 'absolute');


for sample=1:numSamples
    assertElementsAlmostEqual(ddsx(:,sample), Asxx(:,:,sample) * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(eesx(:,sample), Asxx(:,:,sample) * dtsx(:,sample), 'absolute');
    assertElementsAlmostEqual(ffsx(:,sample), Bsxx(:,:,sample) * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(ggsx(:,sample), Bsxx(:,:,sample) * dtsx(:,sample), 'absolute');
    
    assertElementsAlmostEqual(hhsx(sample,:), bsx(sample,:) * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(iisx(sample,:), dsx(sample,:) * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(jjsx(sample,:), bsx(sample,:) * Bsxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(kksx(sample,:), dsx(sample,:) * Bsxx(:,:,sample)', 'absolute');

    assertElementsAlmostEqual(llsx(:,sample), CA * btsx(:,sample), 'absolute');
    assertElementsAlmostEqual(mmsx(:,sample), CA * dtsx(:,sample), 'absolute');
    assertElementsAlmostEqual(nnsx(:,sample), Asxx(:,:,sample) * cbt, 'absolute');
    assertElementsAlmostEqual(oosx(:,sample), Bsxx(:,:,sample) * cbt, 'absolute');
    
    assertElementsAlmostEqual(ppsx(sample,:), bsx(sample,:) * CA', 'absolute');
    assertElementsAlmostEqual(qqsx(sample,:), dsx(sample,:) * CA', 'absolute');
    assertElementsAlmostEqual(rrsx(sample,:), cb * Asxx(:,:,sample)', 'absolute');
    assertElementsAlmostEqual(ttsx(sample,:), cb * Bsxx(:,:,sample)', 'absolute');
end

end


% Test a discrete MATLAB deterministic directed factor
function test4(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

inDomain = 1:10;
[inx,iny] = ndgrid(inDomain,inDomain);
outDomain = sort(unique(reshape(inx + iny,1,length(inDomain)^2)));
a = Discrete(inDomain);
b = Discrete(inDomain);
c = Discrete(outDomain);
fg.addFactor(@myPlusFactor, c, a, b).DirectedTo = c;


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();

as = a.Solver.getAllSamples();
bs = b.Solver.getAllSamples();
cs = c.Solver.getAllSamples();

asx = arrayfun(@(x)x, as);
bsx = arrayfun(@(x)x, bs);
csx = arrayfun(@(x)x, cs);
assertElementsAlmostEqual(asx + bsx, csx, 'absolute');

end

function value = myPlusFactor(out,in1,in2)
    if (out == in1 + in2)
        value = 2;  % Verify that any constant will do
    else
        value = 0;
    end
end


% Test Equals and NotEquals
function test5(debugPrint, repeatable)

numSamples = 100;
scansPerSample = 1;
burnInScans = 0;

fg = FactorGraph();
fg.Solver = 'Gibbs';
fg.Solver.setNumSamples(numSamples);
fg.Solver.setScansPerSample(scansPerSample);
fg.Solver.setBurnInScans(burnInScans);

N = 8;
a = Discrete(1:4);
b = Discrete(1:4);
c = Bit();
d = Bit();
aa = Discrete(1:4,1,N);
ba = Discrete(1:4,1,N);
ca = Bit(1,N);
da = Bit(1,N);

e = Equals(a);
f = Equals(a,b);
g = Equals(a,b,c,d);
h = Equals(a,3);
ea = Equals(aa);
fa = Equals(aa,ba);
ga = Equals(aa,ba,ca,da);
ha = Equals(aa,3);
ia = Equals(a,ba);
ja = Equals(aa,ba,c);

en = NotEquals(a);
fn = NotEquals(a,b);
gn = NotEquals(a,b,c,d);
hn = NotEquals(a,3);
ean = NotEquals(aa);
fan = NotEquals(aa,ba);
gan = NotEquals(aa,ba,ca,da);
han = NotEquals(aa,3);
ian = NotEquals(a,ba);
jan = NotEquals(aa,ba,c);


if (repeatable)
    fg.Solver.setSeed(1);					% Make this repeatable
end
fg.Solver.saveAllSamples();
fg.solve();


aS = a.Solver.getAllSamples();
bS = b.Solver.getAllSamples();
cS = c.Solver.getAllSamples();
dS = d.Solver.getAllSamples();
aaS = aa.invokeSolverMethodWithReturnValue('getAllSamples');
baS = ba.invokeSolverMethodWithReturnValue('getAllSamples');
caS = ca.invokeSolverMethodWithReturnValue('getAllSamples');
daS = da.invokeSolverMethodWithReturnValue('getAllSamples');

eS = e.Solver.getAllSamples();
fS = f.Solver.getAllSamples();
gS = g.Solver.getAllSamples();
hS = h.Solver.getAllSamples();
eaS = ea.invokeSolverMethodWithReturnValue('getAllSamples');
faS = fa.invokeSolverMethodWithReturnValue('getAllSamples');
gaS = ga.invokeSolverMethodWithReturnValue('getAllSamples');
haS = ha.invokeSolverMethodWithReturnValue('getAllSamples');
iaS = ia.invokeSolverMethodWithReturnValue('getAllSamples');
jaS = ja.invokeSolverMethodWithReturnValue('getAllSamples');

enS = en.Solver.getAllSamples();
fnS = fn.Solver.getAllSamples();
gnS = gn.Solver.getAllSamples();
hnS = hn.Solver.getAllSamples();
eanS = ean.invokeSolverMethodWithReturnValue('getAllSamples');
fanS = fan.invokeSolverMethodWithReturnValue('getAllSamples');
ganS = gan.invokeSolverMethodWithReturnValue('getAllSamples');
hanS = han.invokeSolverMethodWithReturnValue('getAllSamples');
ianS = ian.invokeSolverMethodWithReturnValue('getAllSamples');
janS = jan.invokeSolverMethodWithReturnValue('getAllSamples');


aSX = arrayfun(@(x)x, aS);
bSX = arrayfun(@(x)x, bS);
cSX = arrayfun(@(x)x, cS);
dSX = arrayfun(@(x)x, dS);
aSXN = repmat(aSX, 1, N);
bSXN = repmat(bSX, 1, N);
cSXN = repmat(cSX, 1, N);
dSXN = repmat(dSX, 1, N);
aaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), aaS, 'UniformOutput', false));
baSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), baS, 'UniformOutput', false));
caSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), caS, 'UniformOutput', false));
daSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), daS, 'UniformOutput', false));

eSX = arrayfun(@(x)x, eS);
fSX = arrayfun(@(x)x, fS);
gSX = arrayfun(@(x)x, gS);
hSX = arrayfun(@(x)x, hS);
eaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), eaS, 'UniformOutput', false));
faSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), faS, 'UniformOutput', false));
gaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), gaS, 'UniformOutput', false));
haSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), haS, 'UniformOutput', false));
iaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), iaS, 'UniformOutput', false));
jaSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), jaS, 'UniformOutput', false));

enSX = arrayfun(@(x)x, enS);
fnSX = arrayfun(@(x)x, fnS);
gnSX = arrayfun(@(x)x, gnS);
hnSX = arrayfun(@(x)x, hnS);
eanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), eanS, 'UniformOutput', false));
fanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), fanS, 'UniformOutput', false));
ganSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ganS, 'UniformOutput', false));
hanSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), hanS, 'UniformOutput', false));
ianSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), ianS, 'UniformOutput', false));
janSX = cell2mat(cellfun(@(x)(arrayfun(@(y)y,x)), janS, 'UniformOutput', false));


assert(all(eSX));            
assert(all(fSX == (aSX==bSX)));            
assert(all(gSX == (aSX==bSX & aSX==cSX & aSX==dSX)));            
assert(all(hSX == (aSX==3)));          

assert(all(eaSX(:)));
assert(all(faSX(:) == (aaSX(:)==baSX(:))));
assert(all(gaSX(:) == (aaSX(:)==baSX(:) & aaSX(:)==caSX(:) & aaSX(:)==daSX(:))));            
assert(all(haSX(:) == (aaSX(:)==3)));          
assert(all(iaSX(:) == (aSXN(:)==baSX(:))));          
assert(all(jaSX(:) == (aaSX(:)==baSX(:) & aaSX(:)==cSXN(:))));            

assert(all(~enSX));            
assert(all(fnSX ~= (aSX==bSX)));            
assert(all(gnSX ~= (aSX==bSX & aSX==cSX & aSX==dSX)));            
assert(all(hnSX ~= (aSX==3)));            

assert(all(~eanSX(:)));
assert(all(fanSX(:) ~= (aaSX(:)==baSX(:))));
assert(all(ganSX(:) ~= (aaSX(:)==baSX(:) & aaSX(:)==caSX(:) & aaSX(:)==daSX(:))));            
assert(all(hanSX(:) ~= (aaSX(:)==3)));          
assert(all(ianSX(:) ~= (aSXN(:)==baSX(:))));          
assert(all(janSX(:) ~= (aaSX(:)==baSX(:) & aaSX(:)==cSXN(:))));            


end