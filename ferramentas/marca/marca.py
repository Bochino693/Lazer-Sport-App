# -*- coding: utf-8 -*-
"""Fonte unica da marca Lazer & Sport: alvo + dardo.

Emite o mesmo desenho em SVG (para conferir com o olho) e em vector
drawable do Android (para o app). Escrever os dois a partir da mesma
tabela de formas e' o que impede o icone e a abertura de divergirem.

Espaco de coordenadas: 108 x 108, o mesmo do icone adaptativo. O alvo
fica em (48,60) com raio 27 e o dardo entra a 45 graus pela direita,
com a ponta cravada no centro do alvo. Tudo cabe no circulo seguro de
raio 36 -- foi conferido ponto a ponto, e e' por isso que nenhuma
mascara de launcher corta a arte.
"""

# ---------------------------------------------------------------- cores
CRIMSON       = "#E82F4F"   # anel do alvo, o vermelho da marca
CRIMSON_RIM   = "#A8183A"   # aro externo, so' para destacar do fundo
BRANCO        = "#FFFFFF"
AZUL_DARDO    = "#3FC0F0"   # penas e esfera do dardo
AZUL_ESCURO   = "#1B2A44"   # ponta e contorno
FUNDO_TOPO    = "#0C2E5E"
FUNDO_BASE    = "#050F1F"

CENTRO = (48.0, 60.0)


def circulo(cx, cy, r):
    """Circulo como pathData -- vector drawable nao tem <circle>."""
    return (
        f"M{cx - r:.2f},{cy:.2f} "
        f"a{r:.2f},{r:.2f} 0 1,0 {2 * r:.2f},0 "
        f"a{r:.2f},{r:.2f} 0 1,0 {-2 * r:.2f},0 Z"
    )


def poligono(pontos):
    p = " ".join(f"L{x:.2f},{y:.2f}" for x, y in pontos[1:])
    return f"M{pontos[0][0]:.2f},{pontos[0][1]:.2f} {p} Z"


# ------------------------------------------------------------- o alvo
# Aneis de fora para dentro. O aro escuro so' existe para o alvo nao
# encostar no fundo azul sem uma borda que o separe.
ALVO = [
    (circulo(*CENTRO, 27.0),  CRIMSON_RIM),
    (circulo(*CENTRO, 26.0),  CRIMSON),
    (circulo(*CENTRO, 20.5),  BRANCO),
    (circulo(*CENTRO, 15.0),  CRIMSON),
    (circulo(*CENTRO, 9.5),   BRANCO),
    (circulo(*CENTRO, 4.5),   CRIMSON),
]

# ------------------------------------------------------------ o dardo
# Desenhado deitado, apontando para a esquerda com a ponta em (48,60),
# e depois girado -45 graus em torno da propria ponta. Assim a ponta
# fica cravada no centro do alvo sem nenhuma conta de trigonometria
# espalhada pelo arquivo: quem gira e' o grupo.
DARDO_ROTACAO = -45.0
DARDO = [
    # penas, primeiro, para o cano passar por cima da raiz delas
    (poligono([(74, 58.2), (90, 48.0), (89, 57.6)]),   AZUL_DARDO),
    (poligono([(74, 61.8), (90, 72.0), (89, 62.4)]),   AZUL_DARDO),
    # cano branco
    ("M65.50,56.20 L84.00,56.20 A3.80,3.80 0 0 1 84.00,63.80 "
     "L65.50,63.80 Z",                                  BRANCO),
    # esfera azul entre a ponta e o cano
    (circulo(61.5, 60.0, 5.4),                          AZUL_DARDO),
    # ponta: agulha escura cravando o alvo
    (poligono([(48, 60), (60, 57.3), (60, 62.7)]),      AZUL_ESCURO),
]


# Sombra do dardo sobre o alvo. Sao as mesmas formas em preto
# translucido, num grupo identico deslocado alguns pontos: vector
# drawable nao tem desfoque, e sem nada o dardo fica colado no alvo
# como um adesivo.
SOMBRA = "#38000000"
DARDO_SOMBRA_DX = 2.5
DARDO_SOMBRA_DY = 3.0
