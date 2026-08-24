# Publicação na Google Play

Material e informações da ficha do aplicativo.

## Arquivos

| Arquivo | Uso | Exigência da loja |
|---|---|---|
| `icone-512.png` | Ícone da ficha | 512×512, PNG de 32 bits, **sem** transparência |
| `banner-1024x500.png` | Gráfico de destaque | 1024×500, JPG ou PNG sem transparência |

As capturas de tela precisam sair de um aparelho ou emulador: no mínimo
duas por formato, entre 320 px e 3840 px de lado, proporção até 2:1.
Sugestão de telas: abertura, catálogo, detalhe do brinquedo, carrinho.

## Identidade

- **Nome:** Lazer & Sport
- **Pacote:** `br.com.lazersport.app` — imutável depois da primeira publicação
- **Categoria sugerida:** Compras
- **Site:** https://www.lazersport.com.br
- **E-mail de contato:** comercial@lazersport.com.br

## Descrição curta (até 80 caracteres)

Brinquedos, peças e serviços da Lazer & Sport na palma da mão.

## Descrição completa (até 4000 caracteres)

O aplicativo oficial da Lazer & Sport Brinquedos: fábrica e loja de
brinquedos mecânicos, eletrônicos, esportivos, fliperama e simuladores
para shoppings, quiosques e parques.

No aplicativo você pode:

- navegar pelo catálogo completo de brinquedos, peças de reposição,
  combos e promoções;
- ver fotos, medidas, voltagem e descrição de cada equipamento;
- montar seu carrinho e finalizar a compra com PIX ou cartão;
- acompanhar seus pedidos;
- abrir chamados de manutenção e acompanhar o andamento;
- falar direto com a equipe pelo WhatsApp.

Entre com sua conta do site ou com o Google — é a mesma conta nos dois
lugares.

## Gerar o pacote de publicação

1. Crie a chave de assinatura (uma vez só, e **guarde**: sem ela não há
   como atualizar o aplicativo depois):

   ```
   keytool -genkey -v -keystore lazer-sport.jks -keyalg RSA \
     -keysize 2048 -validity 10000 -alias lazersport
   ```

2. Crie `keystore.properties` na raiz do projeto — o `.gitignore` já
   impede que ele seja versionado:

   ```
   storeFile=/caminho/absoluto/lazer-sport.jks
   storePassword=...
   keyAlias=lazersport
   keyPassword=...
   ```

3. Gere o Android App Bundle:

   ```
   ./gradlew bundleRelease
   ```

   O arquivo sai em `app/build/outputs/bundle/release/app-release.aab`.

## Antes de enviar

- [ ] Rodar o `bundleRelease` e instalar em um aparelho de verdade
- [ ] Percorrer: abertura, login com e-mail, login com Google, catálogo,
      detalhe, carrinho e checkout
- [ ] Só depois disso considerar ligar o R8 (`isMinifyEnabled`), testando
      o release outra vez — ofuscação quebra em execução, não no build
- [ ] Preencher o formulário de segurança de dados: o aplicativo coleta
      nome, e-mail e telefone, e usa o Google como opção de login
- [ ] Publicar a política de privacidade e informar a URL na ficha
