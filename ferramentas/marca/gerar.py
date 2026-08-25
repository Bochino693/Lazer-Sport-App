# -*- coding: utf-8 -*-
"""Escreve os vetores do Android e os SVG de conferencia."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from marca import (ALVO, BRANCO, CENTRO, DARDO, DARDO_ROTACAO,
                   DARDO_SOMBRA_DX, DARDO_SOMBRA_DY, FUNDO_BASE,
                   FUNDO_TOPO, SOMBRA, circulo, poligono)

AQUI = os.path.dirname(os.path.abspath(__file__))
# ferramentas/marca -> raiz do projeto -> res do app
RAIZ = os.path.dirname(os.path.dirname(AQUI))
APP = os.path.join(RAIZ, "app", "src", "main", "res")
SCRATCH = AQUI

CABECALHO = """<?xml version="1.0" encoding="utf-8"?>
<!-- GERADO a partir de marca.py — nao edite a mao: o icone e a tela de
     abertura saem do mesmo desenho, e mexer so' aqui faz os dois
     divergirem. -->
"""


def vetor(corpo, largura=108, altura=108):
    return (
        CABECALHO
        + '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{largura}dp"\n'
        f'    android:height="{altura}dp"\n'
        f'    android:viewportWidth="{largura}"\n'
        f'    android:viewportHeight="{altura}">\n'
        + corpo
        + "</vector>\n"
    )


def caminhos_android(formas, recuo="    "):
    saida = ""
    for d, cor in formas:
        saida += (
            f"{recuo}<path\n"
            f'{recuo}    android:fillColor="{cor}"\n'
            f'{recuo}    android:pathData="{d}" />\n'
        )
    return saida


def grupo_sombra(recuo="    "):
    """Mesmo dardo, chapado em preto translucido e deslocado.

    Vai dentro de dois grupos: o de fora so' recorta a sombra ao disco do
    alvo -- e' sombra projetada sobre o alvo, nao mancha solta no fundo --
    e o de dentro gira e desloca. Separados porque o recorte de um grupo
    sofre a transformacao dele; junto, o circulo do recorte giraria e
    sairia do lugar.
    """
    formas = [(d, SOMBRA) for d, _ in DARDO]
    dentro = recuo + "    "
    return (
        f"{recuo}<group>\n"
        f'{dentro}<clip-path android:pathData="{circulo(*CENTRO, 26.0)}" />\n'
        f"{dentro}<group\n"
        f'{dentro}    android:rotation="{DARDO_ROTACAO}"\n'
        f'{dentro}    android:pivotX="{CENTRO[0]}"\n'
        f'{dentro}    android:pivotY="{CENTRO[1]}"\n'
        f'{dentro}    android:translateX="{DARDO_SOMBRA_DX}"\n'
        f'{dentro}    android:translateY="{DARDO_SOMBRA_DY}">\n'
        + caminhos_android(formas, dentro + "    ")
        + f"{dentro}</group>\n"
        f"{recuo}</group>\n"
    )


def grupo_dardo(recuo="    "):
    return (
        f"{recuo}<group\n"
        f'{recuo}    android:name="dardo"\n'
        f'{recuo}    android:rotation="{DARDO_ROTACAO}"\n'
        f'{recuo}    android:pivotX="{CENTRO[0]}"\n'
        f'{recuo}    android:pivotY="{CENTRO[1]}">\n'
        + caminhos_android(DARDO, recuo + "    ")
        + f"{recuo}</group>\n"
    )


# ---------------------------------------------------------------- SVG
def svg(formas_planas, dardo=True, fundo=None, tamanho=108):
    partes = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{tamanho}" '
        f'height="{tamanho}" viewBox="0 0 108 108">'
    ]
    if fundo:
        partes.append(
            '<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">'
            f'<stop offset="0" stop-color="{fundo[0]}"/>'
            f'<stop offset="1" stop-color="{fundo[1]}"/>'
            "</linearGradient></defs>"
            '<rect width="108" height="108" fill="url(#g)"/>'
        )
    for d, cor in formas_planas:
        partes.append(f'<path d="{d}" fill="{cor}"/>')
    if dardo:
        partes.append(
            f'<clipPath id="disco"><path d="{circulo(*CENTRO, 26.0)}"/>'
            "</clipPath>"
            f'<g clip-path="url(#disco)"><g transform="translate('
            f'{DARDO_SOMBRA_DX} {DARDO_SOMBRA_DY}) '
            f'rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">'
        )
        for d, _ in DARDO:
            partes.append(f'<path d="{d}" fill="#000000" fill-opacity="0.22"/>')
        partes.append("</g></g>")
        partes.append(
            f'<g transform="rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">'
        )
        for d, cor in DARDO:
            partes.append(f'<path d="{d}" fill="{cor}"/>')
        partes.append("</g>")
    partes.append("</svg>")
    return "".join(partes)


# ------------------------------------------------------------- arquivos
def escrever(caminho, conteudo):
    os.makedirs(os.path.dirname(caminho), exist_ok=True)
    with open(caminho, "w", encoding="utf-8") as f:
        f.write(conteudo)
    print("escrito", caminho)


# 1. fundo do icone: gradiente diagonal, nada de branco
fundo = vetor(
    "    <path\n"
    '        android:pathData="M0,0 h108 v108 h-108 Z">\n'
    "        <aapt:attr name=\"android:fillColor\">\n"
    '            <gradient xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '                android:type="linear"\n'
    '                android:startX="0" android:startY="0"\n'
    '                android:endX="108" android:endY="108">\n'
    f'                <item android:offset="0" android:color="{FUNDO_TOPO}" />\n'
    f'                <item android:offset="1" android:color="{FUNDO_BASE}" />\n'
    "            </gradient>\n"
    "        </aapt:attr>\n"
    "    </path>\n"
).replace(
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    xmlns:aapt="http://schemas.android.com/aapt"',
)
escrever(f"{APP}/drawable/ls_icone_fundo.xml", fundo)

# 2. simbolo completo (alvo + dardo) -- primeiro plano do icone
escrever(
    f"{APP}/drawable/ls_marca_simbolo.xml",
    vetor(caminhos_android(ALVO) + grupo_sombra() + grupo_dardo()),
)

# 3. alvo e dardo separados -- a abertura anima um sobre o outro
escrever(f"{APP}/drawable/ls_marca_alvo.xml", vetor(caminhos_android(ALVO)))
escrever(f"{APP}/drawable/ls_marca_dardo.xml",
         vetor(grupo_sombra() + grupo_dardo()))

# 4. monocromatico: silhueta de uma cor so'. Os aneis viram rosquinhas
#    com evenOdd, senao o alvo vira um disco chapado.
def rosquinha(r_ext, r_int):
    return circulo(*CENTRO, r_ext) + " " + circulo(*CENTRO, r_int)

mono_paths = [
    (rosquinha(27.0, 20.5), BRANCO),
    (rosquinha(15.0, 9.5), BRANCO),
    (circulo(*CENTRO, 4.5), BRANCO),
]
mono_corpo = ""
for d, cor in mono_paths:
    mono_corpo += (
        "    <path\n"
        f'        android:fillColor="{cor}"\n'
        '        android:fillType="evenOdd"\n'
        f'        android:pathData="{d}" />\n'
    )
# dardo do monocromatico: um so' vulto, sem as cores internas
mono_dardo = [
    (poligono([(76, 58.5), (90, 49.5), (88, 58.0)]), BRANCO),
    (poligono([(76, 61.5), (90, 70.5), (88, 62.0)]), BRANCO),
    ("M65.50,56.20 L84.00,56.20 A3.80,3.80 0 0 1 84.00,63.80 "
     "L65.50,63.80 Z", BRANCO),
    (circulo(61.5, 60.0, 5.4), BRANCO),
    (poligono([(48, 60), (59, 56.6), (59, 63.4)]), BRANCO),
]
mono_corpo += (
    f'    <group android:rotation="{DARDO_ROTACAO}"\n'
    f'        android:pivotX="{CENTRO[0]}" android:pivotY="{CENTRO[1]}">\n'
    + caminhos_android(mono_dardo, "        ")
    + "    </group>\n"
)
escrever(f"{APP}/drawable/ls_marca_mono.xml", vetor(mono_corpo))

# 5. SVG de conferencia
escrever(f"{SCRATCH}/icone.svg", svg(ALVO, fundo=(FUNDO_TOPO, FUNDO_BASE)))
escrever(f"{SCRATCH}/simbolo.svg", svg(ALVO))
escrever(f"{SCRATCH}/alvo.svg", svg(ALVO, dardo=False))

# SVG do dardo sozinho -- e' o que a previa da animacao move sobre o alvo,
# do mesmo jeito que o app move ls_marca_dardo sobre ls_marca_alvo.
partes = [
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">',
    f'<g transform="rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">',
]
for d, cor in DARDO:
    partes.append(f'<path d="{d}" fill="{cor}"/>')
partes.append("</g></svg>")
escrever(f"{SCRATCH}/dardo.svg", "".join(partes))

# sombra do dardo, recortada no disco -- vai entre alvo e dardo
partes = [
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">',
    f'<clipPath id="d"><path d="{circulo(*CENTRO, 26.0)}"/></clipPath>',
    f'<g clip-path="url(#d)"><g transform="translate({DARDO_SOMBRA_DX} '
    f'{DARDO_SOMBRA_DY}) rotate({DARDO_ROTACAO} {CENTRO[0]} {CENTRO[1]})">',
]
for d, _ in DARDO:
    partes.append(f'<path d="{d}" fill="#000000" fill-opacity="0.22"/>')
partes.append("</g></g></svg>")
escrever(f"{SCRATCH}/dardo_sombra.svg", "".join(partes))
