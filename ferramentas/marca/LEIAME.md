# A marca em código

O alvo e o dardo da Lazer & Sport são desenhados aqui, e não num editor
de imagem. O motivo é simples: a mesma arte precisa aparecer em três
lugares que não se falam sozinhos —

* o ícone adaptativo do launcher (`res/drawable/ls_marca_*.xml`),
* a tela de abertura, que anima alvo e dardo separados,
* os PNG do launcher para Android 7, que não tem ícone adaptativo.

Mantida a mão, em algum momento alguém ajusta um dos três e os outros
ficam para trás. Aqui os três saem da mesma tabela de formas.

## Arquivos

| arquivo | o que faz |
|---|---|
| `marca.py` | a geometria: cores, anéis do alvo, peças do dardo. **É o único arquivo que se edita para mudar o desenho.** |
| `gerar.py` | escreve os vector drawables em `app/src/main/res/drawable/` e os SVG de conferência |
| `legado.py` | escreve os SVG do ícone quadrado e redondo do Android 7 |
| `render.py` | rasteriza SVG em PNG com o Chromium (usado pelo `legado.py`) |

## Como regerar

```bash
cd ferramentas/marca
python3 gerar.py           # vetores do app + SVG de conferência
python3 legado.py          # SVG dos ícones antigos
python3 render.py legado_quadrado.svg:q_mdpi.png:48   # etc, por densidade
```

`render.py` precisa do `playwright` (`pip install playwright`) e de um
Chromium. Só os PNG por densidade dependem dele; os vetores, que são o
que o app usa na prática, saem só com `gerar.py`.

## O espaço de coordenadas

Tudo vive num quadro de 108 × 108, o mesmo do ícone adaptativo. O alvo
fica em (48, 60) com raio 27 e o dardo entra a 45 graus pela direita, com
a ponta no centro do alvo.

Os extremos da arte foram conferidos ponto a ponto contra o círculo
seguro de raio 36 centrado em (54, 54): a ponta das penas chega a 35,6 e
a borda do alvo a 35,5. É isso que garante que nenhuma máscara de
launcher corte o dardo — e é o que precisa ser reconferido se o desenho
mudar de tamanho.

O mesmo enquadramento serve à abertura do sistema no Android 12+, que
mostra só os 2/3 centrais do desenho: 72 de 108 é exatamente a área
segura, então o alvo preenche todo o espaço que a plataforma permite.
