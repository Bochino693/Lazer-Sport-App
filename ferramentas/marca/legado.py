# -*- coding: utf-8 -*-
"""PNG do launcher para Android 7 (API 24/25), que nao tem icone adaptativo.

Aqui a arte e' enquadrada mais apertada do que no adaptativo: sem
mascara do sistema para cortar, o circulo seguro de 36 nao faz sentido e
deixaria a marca pequena no meio de uma moldura vazia.
"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from marca import ALVO, CENTRO, DARDO, DARDO_ROTACAO, DARDO_SOMBRA_DX, \
    DARDO_SOMBRA_DY, FUNDO_BASE, FUNDO_TOPO, circulo

CORTE = (10, 10, 88, 88)   # x, y, largura, altura do recorte na arte 108


def corpo():
    p = []
    for d, cor in ALVO:
        p.append(f'<path d="{d}" fill="{cor}"/>')
    p.append(f'<clipPath id="disco"><path d="{circulo(*CENTRO, 26.0)}"/></clipPath>')
    p.append(f'<g clip-path="url(#disco)"><g transform="translate('
             f'{DARDO_SOMBRA_DX} {DARDO_SOMBRA_DY}) '
             f'rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">')
    for d, _ in DARDO:
        p.append(f'<path d="{d}" fill="#000000" fill-opacity="0.22"/>')
    p.append("</g></g>")
    p.append(f'<g transform="rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">')
    for d, cor in DARDO:
        p.append(f'<path d="{d}" fill="{cor}"/>')
    p.append("</g>")
    return "".join(p)


def svg(redondo):
    x, y, w, h = CORTE
    if redondo:
        recorte = f'<circle cx="{x + w / 2}" cy="{y + h / 2}" r="{w / 2}"/>'
    else:
        recorte = (f'<rect x="{x}" y="{y}" width="{w}" height="{h}" '
                   f'rx="{w * 0.22:.1f}"/>')
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{x} {y} {w} {h}">'
        '<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">'
        f'<stop offset="0" stop-color="{FUNDO_TOPO}"/>'
        f'<stop offset="1" stop-color="{FUNDO_BASE}"/></linearGradient>'
        f'<clipPath id="moldura">{recorte}</clipPath></defs>'
        '<g clip-path="url(#moldura)">'
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="url(#g)"/>'
        + corpo() + "</g></svg>"
    )


for nome, redondo in (("legado_quadrado", False), ("legado_redondo", True)):
    with open(f"{nome}.svg", "w", encoding="utf-8") as f:
        f.write(svg(redondo))
    print("escrito", nome)
