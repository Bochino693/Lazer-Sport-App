import asyncio, os, sys
from playwright.async_api import async_playwright

async def main(pares):
    async with async_playwright() as p:
        b = await p.chromium.launch(
            executable_path="/opt/pw-browsers/chromium-1194/chrome-linux/chrome")
        for svg, png, tam in pares:
            html = (
                "<style>html,body{margin:0;padding:0;background:transparent}"
                f"svg{{width:{tam}px;height:{tam}px;display:block}}</style>"
                + open(svg, encoding="utf-8").read()
            )
            pg = await b.new_page(viewport={"width": tam, "height": tam},
                                  device_scale_factor=1)
            await pg.set_content(html)
            await pg.screenshot(path=png, omit_background=True)
            await pg.close()
            print("render", png, tam)
        await b.close()

pares = [tuple(a.split(":")) for a in sys.argv[1:]]
asyncio.run(main([(s, p, int(t)) for s, p, t in pares]))
