import json, numpy as np, wave
SR = 48000; DUR = 32.0
rng = np.random.default_rng(7)
out = np.zeros((int(SR * DUR), 2))
def env(n, a, d):
    t = np.arange(n) / SR
    return np.minimum(1, t / a) * np.exp(-t / d)
def tone(f, n, a=0.002, d=0.08, f_end=None):
    t = np.arange(n) / SR
    ff = f if f_end is None else np.linspace(f, f_end, n)
    ph = 2 * np.pi * np.cumsum(ff) / SR
    return np.sin(ph) * env(n, a, d)
def lp(x, k):  # simple one-pole lowpass
    y = np.zeros_like(x); a = k; acc = 0.0
    for i, v in enumerate(x): acc += a * (v - acc); y[i] = acc
    return y
def make(kind):
    if kind == 'tick':   return 0.22 * tone(1800, int(.06*SR), d=.012) + 0.12 * tone(900, int(.06*SR), d=.02)
    if kind == 'pop':    return 0.35 * tone(520, int(.25*SR), d=.06, f_end=880)
    if kind == 'bubble': return 0.28 * tone(700 + rng.integers(0, 500), int(.2*SR), d=.045, f_end=1400)
    if kind == 'click':  return 0.35 * tone(2400, int(.08*SR), d=.008) + 0.25 * tone(300, int(.08*SR), d=.02)
    if kind == 'strike':
        n = int(.35*SR); x = rng.standard_normal(n); x = lp(x, .08) * env(n, .02, .09); return 0.6 * x
    if kind == 'whoosh':
        n = int(.6*SR); x = rng.standard_normal(n); t = np.arange(n)/SR
        x = lp(x, .05) * np.sin(np.pi * t / t[-1]) ** 2; return 0.9 * x
    if kind == 'chime':
        n = int(1.6*SR); return 0.18*tone(784, n, d=.5) + 0.13*tone(1175, n, a=.01, d=.45) + 0.09*tone(1568, n, a=.02, d=.35)
    raise ValueError(kind)
cues = json.load(open('frames/sfx.json'))
for i, c in enumerate(cues):
    s = make(c['type']) * c['gain']
    st = int(c['t'] * SR); pan = 0.5 + 0.25 * np.sin(i * 1.7)
    e = min(len(out), st + len(s)); s = s[:e - st]
    out[st:e, 0] += s * (1 - pan) * 1.4; out[st:e, 1] += s * pan * 1.4
# gentle reverb-ish tail
d = int(.09 * SR); wet = np.zeros_like(out); wet[d:] = out[:-d] * .25; wet[2*d:] += out[:-2*d] * .12
out = out + wet
out = out / max(1e-6, np.abs(out).max()) * 0.7
pcm = (out * 32767).astype('<i2')
with wave.open('sfx.wav', 'wb') as w:
    w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
print(len(cues), 'cues')
