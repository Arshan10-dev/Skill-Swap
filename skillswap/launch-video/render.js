const { chromium } = require(process.env.PW || 'playwright');
const fs = require('fs');
const [,, mode, out] = process.argv; // mode: "test" or "all"
(async () => {
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1920, height: 1080 } });
  p.on('pageerror', (e) => console.log('PAGEERROR', e.message));
  await p.goto('http://localhost:4200/index.html');
  await p.evaluate(() => document.fonts.ready);
  await p.waitForTimeout(800);
  fs.mkdirSync(out, { recursive: true });
  const fps = 30, dur = await p.evaluate(() => window.DURATION);
  const times = mode === 'test' ? (process.argv[4] || '').split(',').map(Number) : [...Array(Math.round(dur * fps)).keys()].map((f) => f / fps);
  let i = 0;
  const from = +(process.env.FROM || 0);
  for (const t of times) {
    if (mode !== 'test' && i < from) { i++; continue; }
    await p.evaluate((t) => window.seek(t), t);
    await p.screenshot({ path: `${out}/${mode === 'test' ? 't' + t.toFixed(2) : String(i).padStart(4, '0')}.jpg`, type: 'jpeg', quality: 92 });
    i++;
  }
  fs.writeFileSync(out + '/sfx.json', JSON.stringify(await p.evaluate(() => window.SFX)));
  await b.close();
})();
