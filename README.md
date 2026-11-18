<div align="center">

<img width="100%" alt="axminions-banner" src="https://github.com/user-attachments/assets/384c8403-801e-431c-8bab-30a9e60aec14" />

# AxMinions Fork — `1.0.50-GS-DEFINITIVA`

**Comparação Completa e Expandida com o Repositório Oficial [Artillex-Studios/AxMinions](https://github.com/Artillex-Studios/AxMinions)**  
*Commit Base Oficial (`HEAD`): `7339580` ("Add 26.3 support")*

[![Minecraft](https://img.shields.io/badge/Minecraft%20%2F%20Paper-26.3-brightgreen?style=for-the-badge&logo=minecraft)](#)
[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#)
[![Issues](https://img.shields.io/badge/Bug%20Reports-GitHub%20Issues-red?style=for-the-badge&logo=github)](https://github.com/Artillex-Studios/Issues)
[![Discord](https://img.shields.io/badge/Support-Discord-5865F2?style=for-the-badge&logo=discord)](https://dc.artillex-studios.com/)

</div>

---

## 📑 Sumário

* **[PARTE 1 — Resumo para Usuário e Guia Prático de Uso](#-parte-1--resumo-para-usuário-e-guia-prático-de-uso)**
  * [1.1 O que é este Fork e para que ele serve](#11-o-que-é-este-fork-e-para-que-ele-serve)
  * [1.2 Principais Novidades (Visão Geral Simples)](#12-principais-novidades-visão-geral-simples)
  * [1.3 Como Usar os Novos Sistemas (Passo a Passo)](#13-como-usar-os-novos-sistemas-passo-a-passo)
  * [1.4 Árvore de Habilidades e Níveis 11 a 20 (Os 8 Minions)](#14-árvore-de-habilidades-e-níveis-11-a-20-os-8-minions)
  * [1.5 Sistema de Drops Customizados (Fisher e Lumber)](#15-sistema-de-drops-customizados-fisher-e-lumber)
  * [1.6 Traduções e 11 Idiomas Nativos](#16-traduções-e-11-idiomas-nativos)
* **[PARTE 2 — Relatório Técnico Detalhado (Classe, Função e Linha por Linha)](#-parte-2--relatório-técnico-detalhado-classe-função-e-linha-por-linha)**
  * [2.1 Estatísticas Gerais do Git Diff vs. Oficial (`7339580`)](#21-estatísticas-gerais-do-git-diff-vs-oficial-7339580)
  * [2.2 Build System, Gradle, Java 25, Dependências e `.gitignore`](#22-build-system-gradle-java-25-dependências-e-gitignore)
  * [2.3 Módulo `:api` (Interfaces, Modelos de Habilidades, Menus e Config)](#23-módulo-api-interfaces-modelos-de-habilidades-menus-e-config)
  * [2.4 Módulo `:common` — Núcleo, Comandos, H2 Database, Minions e Ticker](#24-módulo-common--núcleo-comandos-h2-database-minions-e-ticker)
  * [2.5 Módulo `:common` — Listeners e Proteções](#25-módulo-common--listeners-e-proteções)
  * [2.6 Módulo `:common` — Os 8 MinionTypes e NMS Interface](#26-módulo-common--os-8-miniontypes-e-nms-interface)
  * [2.7 Módulo `:common` — Novos Subsistemas (Skills, Menus, Logs, Drops, Utils)](#27-módulo-common--novos-subsistemas-skills-menus-logs-drops-utils)
  * [2.8 Módulo `:common` — Integrações (HeroVender, ItemStacker, WildStacker, EliteMobs)](#28-módulo-common--integrações-herovender-itemstacker-wildstacker-elitemobs)
  * [2.9 Módulo `:nms:v26_3` (DamageHandler, LootHandler, DialogMenuHandler, NMSHandler)](#29-módulo-nmsv26_3-damagehandler-loothandler-dialogmenuhandler-nmshandler)
  * [2.10 Arquivos YAML e Traduções (`config.yml`, `messages.yml`, `lang/*`, `minions/*`)](#210-arquivos-yaml-e-traduções-configyml-messagesyml-lang-minions)
* **[🚀 Como Subir este Repositório como Fork no GitHub](#-como-subir-este-repositório-como-fork-no-github)**

---

# 📖 PARTE 1 — Resumo para Usuário e Guia Prático de Uso

## 1.1 O que é este Fork e para que ele serve

Este projeto é uma versão amplamente melhorada, corrigida e expandida do plugin oficial [AxMinions](https://github.com/Artillex-Studios/AxMinions), atualizada para rodar exclusivamente e de forma nativa no **Minecraft/Paper 26.3** com **Java 25** e **Kotlin 2.3.21**.

Enquanto o AxMinions oficial vai apenas até o Nível 10, tem interfaces simples de baú (GUI), poucas integrações e alguns bugs conhecidos em servidores modernos (como falha ao vender itens com tags NBT no HeroVender, perda de dano/drops do Slayer ou baú desvinculado após reiniciar chunks), este fork transforma cada minion em uma máquina de progressão até o **Nível 20**, contando com:
* **Árvore de Habilidades estilo RPG** com **64 habilidades únicas** (8 por minion);
* **Menu Dialog nativo** do Minecraft 1.21.6+ / 26.3;
* **Drops customizados avançados**;
* **Sistema de logs** completo para a staff;
* **Proteção completa** de economia e entidades customizadas.

---

## 1.2 Principais Novidades (Visão Geral Simples)

### 1. Progressão Dobrada: Nível 1 até Nível 20
* O oficial termina no Nível 10. Aqui todos os 8 minions vão até o **Nível 20**, com novos custos de dinheiro e ações, maior armazenamento de XP/dinheiro e cores evolutivas na armadura de couro até o Lv 20.
* Os valores iniciais de velocidade e alcance foram rebalanceados para não deixar minions Lv 1 apelativos demais; o poder verdadeiro agora é conquistado evoluindo o minion e investindo pontos na Árvore de Habilidades.

### 2. Menu Nativo Moderno (Dialog API) + Compatibilidade Automática
* Jogadores no **Java 1.21.6 ou superior (incluindo 26.3)** recebem por padrão o novo **Painel Nativo Dialog** (janela moderna com botões largos, tooltips e ícone em destaque).
* Jogadores no **Bedrock** (via Floodgate/Geyser) ou em **clientes antigos** (via ViaVersion abaixo da 1.21.6) são detectados automaticamente e recebem o **Menu Clássico de Baú (GUI)**, sem travar nem bugar.
* Cada jogador escolhe seu modo preferido com `/axminions menu` ou pelo botão dentro do próprio painel do minion, e a escolha fica salva no banco H2.

### 3. Árvore de Habilidades (Stats + 64 Poderes Especiais) e Respec
* A cada nível do minion, ele ganha pontos de habilidade configuráveis em `points-per-level`.
* Você pode gastar pontos em **Atributos básicos** (*Velocidade, Alcance, Economia de Durabilidade, Armazenamento, Multiplicador de Venda ou Sorte*) **OU** desbloquear **Habilidades Especiais** (*8 por minion = 64 no total!*).
* **Errou a build?** O botão **"Resetar Pontos (Respec)"** devolve todos os pontos gastos mediante uma taxa em dinheiro (padrão: `1.000.000`).

### 4. Recolher Minion COM ou SEM Ferramenta
Ao clicar para recolher o minion, abre-se uma confirmação oferecendo duas opções claras:
* **COM a ferramenta:** a picareta/espada/machado/enxada/vara fica salva em NBT dentro do próprio item do minion (mantendo encantamentos e durabilidade) para guardar facilmente em baús ou vender.
* **SEM a ferramenta:** o minion volta como item limpo e a ferramenta vai separada para o inventário do jogador.

### 5. Drops Customizados para Pescador (`Fisher`) e Lenhador (`Lumber`)
* **Fisher** recebeu **62 drops customizados** (de algas, prismarinho e tridente até itens temáticos, moedas, pergaminhos e baú/caixas de recompensa).
* **Lumber** recebeu **8 drops customizados** (gravetos, maçãs, carvão vegetal, favo de mel, resina `RESIN_CLUMP`, maçã dourada e maçã encantada).
* Suporte a `executable-item` (*ExecutableItems*), comandos no console, sorte extra via *Luck of the Sea* (Sorte do Mar) e controle de máximo por ação (`custom-drops-max-per-catch: 1`).

### 6. Correções Críticas de Servidores Survival / Skyblock
* **Slayer sem bugs:** removeu o "Fox" fantasma do NMS que causava erros de dano; agora protege entidades do *EliteMobs* e *Villagers*/montarias, funciona mesmo sem baú vinculado (quando configurado) e coleta drops no chão ou envia direto para o baú com a skill `coleta_direta`.
* **WildStacker melhorado:** mata múltiplos stacks com a skill `golpe_wildstacker` e calcula XP real usando `WildStacker.getExp(...)`.
* **HeroVender integrado e corrigido:** remove tags internas de NBT do AxMinions (`ItemUtils.stripAxMinionsNbt`) para o item voltar a ser vanilla puro e ser aceito pelos menus do HeroVender, resolvendo loja por dono offline e permissão; **Seller** ainda vende direto de dentro de *Shulker Boxes* no baú!
* **KGenerators atualizado para v7.3** (`KGenerators-7.3-bnull.jar`) com respeito ao modo de quebra manual e proteções *WorldGuard*.
* **Baús em chunks descarregados:** agora o plugin re-sincroniza o baú vinculado automaticamente quando o chunk carrega, evitando perda silenciosa de link.
* **Segurança & Banco H2:** cada minion grava quem foi o **PRIMEIRO DONO** e o **HISTÓRICO DE DONOS** no banco e no NBT do item. Logs diários em `logs/` e comandos de auditoria para a administração (`/axminions admin`, `list`, `inspect`, `logs`, `tp`, `debug`).

---

## 1.3 Como Usar os Novos Sistemas (Passo a Passo)

### 🎮 A) Para Jogadores
1. **Posicionar:** Coloque o minion no chão com o botão direito (ele registra você no histórico de donos e abre automaticamente no futuro).
2. **Interagir:** Clique com o botão direito no minion para abrir o Painel:
   * 🌟 **Habilidades** *(estrela/esmeralda)*: Evolua Velocidade, Alcance, Durabilidade, Armazenamento ou Habilidades Especiais.
   * ⛏️ **Ferramenta**: Coloque ou tire a ferramenta em um menu dedicado de troca (slot central `13`).
   * 📦 **Vincular Baú**: Clique nesta opção e depois clique com o botão esquerdo em um baú para conectá-lo ao minion.
   * 🔄 **Trocar Menu**: Clique no botão ou digite `/axminions menu [gui|dialog]` para alternar entre o Menu Dialog Moderno e o Menu Clássico de Baú.
   * 📥 **Recolher Minion**: Escolha se quer levar a ferramenta junto dentro do item ou devolvida separada no inventário.

### 🛡️ B) Para Administradores

| Comando | Descrição |
| :--- | :--- |
| `/axminions give <tipo> [jogador] [nivel]` | Dá um minion (valida o nível máximo configurado no `.yml` do minion, até Lv 20). |
| `/axminions admin [dono]` | Abre uma GUI de 54 slots listando todos os minions ativos no servidor, mostrando primeiro dono, histórico, baú, ferramenta, ações e XP/dinheiro guardado.<br>• **Clique Esquerdo:** Teleporta até o minion.<br>• **Shift + Clique Direito:** Remove o minion do mundo/banco. |
| `/axminions list [dono] [pagina]` | Lista os minions em texto no chat com teleporte clicável. |
| `/axminions inspect [x y z]` | Inspeciona detalhadamente o minion que você está olhando (ou nas coordenadas informadas): nível, pontos, builds, baú, dono original e histórico. |
| `/axminions logs [quantidade] [filtro]` | Mostra os logs recentes em memória (`placement`, `pickup`, `link`, `tool`, `admin`, `upgrade`, etc.). |
| `/axminions tp <x> <y> <z> [mundo]` | Teleporta até um minion específico. |
| `/axminions debug <subcomando>` | Ferramentas profundas de diagnóstico: `link`, `info`, `abilities`, `xp`, `wood`, `logs`, `sync`, `settool`, `setlevel`, `open`, `checkchest`, `spawnmob`, `unlink`, `break`, `place`. |
| `/axminions reload` | Recarrega configs, idioma (`lang/*.yml`), caches de habilidades, cooldowns do `AbilityEngine`, cache de `MenuRouter` e força a ressincronização de baús vinculados. |

---

## 1.4 Árvore de Habilidades e Níveis 11 a 20 (Os 8 Minions)

Cada arquivo em `common/src/main/resources/minions/*.yml` agora possui:
1. **`upgrades` do nível `1` ao `20`**: Com requisitos de `money` e `actions`, além de `speed`, `range`, `storage`, `multiplier` (para o Seller) e visual da armadura evoluindo até o Lv 20.
2. **`skill-system`**: Define `enabled: true`, `respec-cost-money: 1000000`, tabela de pontos ganhos por nível (`points-per-level`) e `stats` evoluíveis:
   * `speed` (Redstone, slot `10`, max-tier `6`)
   * `range` (slot `11`, max-tier `4`; exceto Crafter/Seller que não usam range)
   * `durability_save` (Anvil, slot `12`, max-tier `4`; exceto Crafter/Seller)
   * `storage` (Experience Bottle / Gold Block, slot `13` ou `11`/`12`, max-tier `5`)
   * **Exclusivos:** Seller tem `multiplier` (Emerald, slot `11`, max-tier `4`); Fisher tem `luck` (Rabbit Foot, slot `12`, max-tier `4`).
3. **`custom-abilities`**: **8 poderes únicos por minion** (slots `19`, `21`, `23`, `25`, `28`, `30`, `32`, `34` da GUI de Habilidades), desbloqueados por nível mínimo (`min-level` entre `8` e `20`):

<details open>
<summary><b>📦 1. COLLECTOR (Coletor)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `aspirador_xp` | Lv 10 | `ON_ACTION` | Coleta orbs de XP próximos. |
| `xp_vacuum` | Lv 11 | `ON_PERIODIC` | Aspiração contínua de XP por ciclo. |
| `void_trash` | Lv 13 | `ON_ACTION` | Apaga itens lixo configurados (podres, ossos, linha, sementes, etc.). |
| `compactador_coleta` | Lv 15 | `ON_ACTION` | Compacta drops em blocos. |
| `compact_chest` | Lv 15 | `ON_PERIODIC` | Comprime todo o baú vinculado. |
| `overflow_teleporter` | Lv 16 | `ON_ACTION` | Se o baú encher, tenta enviar para o EnderChest do dono. |
| `stack_merger` | Lv 18 | `ON_PERIODIC` | Junta stacks soltos no chão para reduzir lag. |
| `dimensional_pull` | Lv 19 | `ON_ACTION` | Amplia o raio dimensional de coleta (`COLLECTOR_RANGE_BOOST`). |

</details>

<details>
<summary><b>🛠️ 2. CRAFTER (Crafteiro)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `forja_lote` | Lv 8 | `ON_ACTION` | Crafta vários itens em lote por ciclo. |
| `multi_output` | Lv 11 | `ON_ACTION` | Chance de saída duplicada (`2x`). |
| `auto_smelt_craft` | Lv 12 | `ON_ACTION` | Assa automaticamente outputs craftáveis. |
| `compact_blocks` | Lv 14 | `ON_ACTION` | Compacta resultados automaticamente. |
| `obra_prima` | Lv 15 | `ON_ACTION` | Chance maior de craft bônus mestre (`2x`). |
| `byproduct_recycle` | Lv 16 | `ON_ACTION` | Devolve ingrediente ao concluir. |
| `master_artisan` | Lv 18 | `ON_ACTION` | Reduz intervalo entre ações (`ASSEMBLY_SPEED_REDUCE`). |
| `assembly_line` | Lv 20 | `ON_ACTION` | Concede XP extra por linha de montagem. |

</details>

<details>
<summary><b>🌾 3. FARMER (Agricultor)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `enfardadeira` | Lv 10 | `ON_ACTION` | Compacta colheitas (ex: trigo $\rightarrow$ fardo). |
| `auto_replant_boost` | Lv 11 | `ON_PERIODIC` | Acelera estágio de crescimento das plantações próximas. |
| `hydration_aura` | Lv 13 | `ON_PERIODIC` | Umidifica terra arada ao redor. |
| `bone_meal_mist` | Lv 14 | `ON_PERIODIC` | Aplica efeito de farinha de osso em área periodicamente. |
| `safra_dupla` | Lv 16 | `ON_ACTION` | Duplica a colheita (`factor: 2.0`). |
| `auto_composter` | Lv 16 | `ON_ACTION` | Converte sementes excedentes em bone meal. |
| `golden_crop` | Lv 18 | `ON_ACTION` | Chance de transformar cenoura/maçã em versão dourada (`GOLDEN_CROP_UPGRADE`). |
| `bountiful_harvest` | Lv 20 | `ON_ACTION` | Adiciona cultivo bônus extra. |

</details>

<details>
<summary><b>🎣 4. FISHER (Pescador)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `rede_dupla` | Lv 12 | `ON_ACTION` | Multiplica os itens pescados (`2x`). |
| `deep_bait` | Lv 12 | `ON_ACTION` | Transforma lixo da pesca em tesouro. |
| `abyssal_chest` | Lv 14 | `ON_ACTION` | Aumenta Sorte do Mar efetiva na pesca. |
| `xp_tide` | Lv 15 | `ON_ACTION` | Multiplica XP obtido (`1.35x` por tier). |
| `instinto_abissal` | Lv 16 | `ON_ACTION` | Aumenta quantidade de rolls em `custom-drops` por pescada. |
| `storm_fisher` | Lv 17 | `ON_ACTION` | Rola pescadas extras quando está chovendo/tempestade no mundo. |
| `kraken_hook` | Lv 18 | `ON_ACTION` | Rola bônus dourado exclusivo do pescador. |
| `golden_school` | Lv 19 | `ON_ACTION` | Cardume dourado triplicando drops (`3x`). |

</details>

<details>
<summary><b>🪓 5. LUMBER (Lenhador)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `fertilidade_muda` | Lv 8 | `ON_ACTION` | Acelera crescimento da muda replantada. |
| `sap_collector` | Lv 11 | `ON_ACTION` | Chance de extrair resinas/seivas (`FOREST_SAP_DROP`). |
| `leaf_decay_instant` | Lv 12 | `ON_ACTION` | Limpa folhas conectadas à árvore automaticamente (`CLEAR_TREE_LEAVES`). |
| `carvoaria_portatil` | Lv 14 | `ON_ACTION` | Assa toras em carvão vegetal. |
| `auto_charcoal` | Lv 15 | `ON_ACTION` | Segundo estágio de queima para carvão. |
| `deep_root` | Lv 17 | `ON_ACTION` | Aumenta multiplicador de drops raros de árvores (`LUMBER_RARE_DROP_BOOST`). |
| `plank_sawmill` | Lv 18 | `ON_ACTION` | Converte toras diretamente em tábuas. |
| `ancient_timber` | Lv 20 | `ON_ACTION` | Duplica madeira colhida (`2x`). |

</details>

<details>
<summary><b>⛏️ 6. MINER (Minerador)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `fornalha_abissal` | Lv 8 | `ON_ACTION` | Auto-smelt em minérios brutos. |
| `xp_magnet` | Lv 11 | `ON_ACTION` | Aumenta XP ganho na mineração (`1.25x` base). |
| `fossil_finder` | Lv 13 | `ON_ACTION` | Encontra fósseis/itens raros do pool de mineração (`BONUS_DROP`). |
| `compact_ore` | Lv 14 | `ON_ACTION` | Compacta minérios em blocos no ato. |
| `compactador_mineral` | Lv 15 | `ON_ACTION` | Smelt + compactação direta. |
| `obsidian_breaker` | Lv 16 | `ON_ACTION` | Drop extra de Obsidian / Crying Obsidian sem gastar durabilidade. |
| `treasure_geode` | Lv 18 | `ON_ACTION` | Encontra geodos/tesouros minerais (`RANDOM_MINERAL_CHEST`). |
| `overdrive_pulse` | Lv 20 | `ON_ACTION` | Chance de pulso elétrico quebrando múltiplos blocos em sequência sem cooldown completo. |

</details>

<details>
<summary><b>💰 7. SELLER (Vendedor)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `tax_evader` | Lv 11 | `ON_ACTION` | `+6%` por tier no valor vendido. |
| `deposito_vault` | Lv 12 | `ON_ACTION` | Envia dinheiro direto para a conta do jogador no Vault em vez de acumular no storage do minion. |
| `bulk_contract` | Lv 13 | `ON_ACTION` | Bônus se vender packs de 64+ itens. |
| `vip_broker` | Lv 15 | `ON_ACTION` | Eleva o multiplicador base de venda. |
| `venda_critica` | Lv 16 | `ON_ACTION` | Chance de venda crítica (`1.25x`). |
| `shulker_liquidation` | Lv 16 | `ON_ACTION` | Bônus percentual quando os itens vêm de dentro de uma Shulker Box no baú. |
| `lucky_coin` | Lv 18 | `ON_ACTION` | Chance de moeda da sorte (`+500$` por tier). |
| `market_surge` | Lv 20 | `ON_ACTION` | Pico de mercado aumentando em `+35%` por tier no momento do proc. |

</details>

<details>
<summary><b>⚔️ 8. SLAYER (Caçador)</b></summary>

| Habilidade (`ID`) | Nível Mín. | Gatilho | Efeito |
| :--- | :---: | :---: | :--- |
| `coleta_direta` | Lv 10 | `ON_ACTION` | Envia os drops do mob morto direto para o baú do minion, evitando ficarem perdidos no chão. |
| `looting_edge` | Lv 11 | `ON_ACTION` | Rola drops extras simulando Looting. |
| `soul_drain` | Lv 13 | `ON_ACTION` | Aumenta o XP drenado dos mobs (`1.28x`). |
| `head_hunter` | Lv 15 | `ON_ACTION` | Chance de derrubar a cabeça do mob morto. |
| `golpe_wildstacker` | Lv 16 | `ON_ACTION` | Abate `+1` stack adicional por tier em mobs empilhados (*WildStacker*). |
| `shockwave_cleave` | Lv 17 | `ON_ACTION` | Atinge `+2` alvos extras ao redor. |
| `rare_essence` | Lv 19 | `ON_ACTION` | Chance de gerar essências raras por tipo de mob. |
| `executioner_frenzy` | Lv 20 | `ON_ACTION` | Chance de golpe duplo imediato. |

</details>

---

## 1.5 Sistema de Drops Customizados (Fisher e Lumber)

No arquivo do minion (`fisher.yml` com **62 entradas** e `lumber.yml` com **8 entradas**), cada drop aceita os seguintes parâmetros:

* `enabled`: `true` / `false`
* `chance`: chance percentual base (ex: `6.0` = 6%, `0.025` = 0.025%)
* `luck-bonus`: bônus % extra somado por ponto de Sorte / *Luck of the Sea*
* `min-level` / `min-luck`: exigência mínima de nível do minion e sorte
* `min-amount` / `max-amount`: faixa aleatória de quantidade
* `replace-vanilla`: se `true`, substitui o drop padrão da ação
* `executable-item`: ID exato de item do plugin *ExecutableItems* (suporta placeholder `<player>`)
* `material`, `name`, `lore`, `glow`, `enchantments`: para itens Bukkit tradicionais
* `commands`: comandos de console suportando `<player>` e `<amount>`

Além disso, `custom-drops-max-per-catch: 1` limita quantos custom drops podem sair por única ação (somado ao bônus da skill `instinto_abissal` no Pescador).

---

## 1.6 Traduções e 11 Idiomas Nativos

O `config.yml` agora define `language: pt_BR` como padrão e `config-version: 11`. Na pasta `common/src/main/resources/lang/`, todos os **11 arquivos de idioma** (156 linhas cada, `config-version: 11`) são extraídos na pasta do plugin ao iniciar:

`ar_SA.yml` • `bn_BD.yml` • `de_DE.yml` • `en_US.yml` • `es_ES.yml` • `fr_FR.yml` • `hi_IN.yml` • `id_ID.yml` • `pt_BR.yml` • `ru_RU.yml` • `zh_CN.yml`

---

# 🔧 PARTE 2 — Relatório Técnico Detalhado (Classe, Função e Linha por Linha)

## 2.1 Estatísticas Gerais do Git Diff vs. Oficial (`7339580`)

* **Baseline oficial:** `Artillex-Studios/AxMinions`, commit `7339580` ("Add 26.3 support").
* **Versão do fork:** `1.0.50-GS-DEFINITIVA` (Minecraft/Paper `26.3`, Java 25, Kotlin 2.3.21).
* **Total de mudanças rastreadas em `git diff --stat`:**
  * **53 arquivos modificados/deletados rastreados:** `6592 insertions(+)`, `1558 deletions(-)`.
  * **2 arquivos removidos vs. upstream:** `.build.gradle.swp` (arquivo temporário Vim que havia vazado no repo oficial) e `libs/KGenerators-7.3-b161.jar`.
  * **27 itens novos não rastreados:** 12 classes/arquivos Kotlin em `api/`, `common/`, `nms/v26_3/`, 11 arquivos YAML em `lang/`, `libs/KGenerators-7.3-bnull.jar`, e este `README`.

> **Nota sobre convenção de linhas neste relatório:**  
> * Formato `HEAD Lx -> NEW Ly`: mostra a localização no arquivo oficial e a nova localização no fork após as modificações.  
> * Arquivos novos possuem apenas as linhas atuais no fork.

---

## 2.2 Build System, Gradle, Java 25, Dependências e `.gitignore`

1. **`build.gradle`** *(diff: 131 linhas alteradas)*
   * Remoção de repositórios duplicados no topo (`mavenLocal`/`central`/`sonatype` duplicados) e blocos multi-versão de NMS antigos (`1.20.1` até `26.2`).
   * Versão alterada para `'1.0.50-GS-DEFINITIVA'`.
   * Remoção de dependência de plugin `io.github.goooler.shadow` / config antiga de `reobf` para subprojetos antigos; configurado `jvmToolchain(25)` para compilar nativamente em Java 25 / Kotlin `2.3.21` visando apenas `:api`, `:common`, `:nms:v26_3`.
   * Remoção de `relocate("org.h2", ...)` do `shadowJar` para manter `org.h2.Driver` no classpath compatível com o `HikariDataSource` sem falhas de reflexão/`ServiceLoader`.
2. **`settings.gradle`** *(diff: 27 linhas alteradas)*
   * Removido bloco `pluginManagement` com `foojay-resolver-convention` e removidos todos os includes antigos (`nms:v1_20_R1`, `R2`, `R3`, `v1_20_R4`, `v1_21`, `v1_21_R2`, `v1_21_R3`, `v1_21_R4`, `v1_21_R5`, `v1_21_R6`, `v1_21_R7`, `v26_1`, `v26_2`).
   * Mantidos apenas: `api`, `common`, `nms`, `nms:v26_3`.
3. **`gradle.properties`** *(diff: +26 linhas)*
   * Adicionados parâmetros de performance e build em Java 25: `org.gradle.jvmargs=-Xmx4G`, `org.gradle.daemon=true`, `org.gradle.parallel=true`, `org.gradle.caching=true`, `org.gradle.configuration-cache=true`, `kotlin.daemon.jvmargs=-Xmx2G`, `kotlin.incremental=true`, `org.gradle.warning.mode=summary`.
4. **`api/build.gradle`** *(diff: 2 linhas)*
   * Atualizada dependência `compileOnly` `org.spigotmc:spigot-api` de `1.20.1-R0.1-SNAPSHOT` para `1.20.6-R0.1-SNAPSHOT` para suportar APIs de meta/enchantments modernos.
5. **`common/build.gradle`** *(diff: 15 linhas, L1-50)*
   * `L12`: substituído `spigot-api:1.20.1-R0.1-SNAPSHOT` por `1.20.6-R0.1-SNAPSHOT`.
   * `L23`: substituída dependência local `../libs/KGenerators-7.3-b161.jar` por `compileOnly files('../libs/KGenerators-7.3-bnull.jar')`.
   * `L27`: adicionado explicitamente `implementation 'com.h2database:h2:2.2.220'`.
   * `L36-46`: capturado `def pluginVersion = version.toString()` fora da closure do `processResources { filesMatching(...) { expand(version: pluginVersion) } }` para compatibilidade total com o Configuration Cache do Gradle.
6. **`.gitignore`** *(diff: +7 linhas, NEW L38-47)*
   * Adicionadas regras `.kotlin/`, `*.swp`, `logs/` antes de `.DS_Store` para manter caches do Kotlin, swaps de editor e logs de execução fora do versionamento Git.
7. **`libs/`**
   * **Deletado:** `libs/KGenerators-7.3-b161.jar`
   * **Adicionado:** `libs/KGenerators-7.3-bnull.jar` (mantido rastreado por ser dependência local obrigatória de compilação do `common/build.gradle:23`).

---

## 2.3 Módulo `:api` (Interfaces, Modelos de Habilidades, Menus e Config)

1. **`api/.../config/Config.kt`** *(+4L)*
   * `NEW L20`: `fun LANGUAGE() = config.getString("language", "pt_BR")` $\rightarrow$ novo seletor de idioma com padrão `pt_BR`.
   * `NEW L28`: `fun USE_DIALOG_MENU() = config.getBoolean("use-dialog-menu", true)` $\rightarrow$ chave mestra do sistema de Painel Dialog nativo.
2. **`api/.../config/Messages.kt`** *(+46L)*
   * `NEW L24-28`: `MENU_MODE_SET()`, `MENU_MODE_BEDROCK()`, `MENU_MODE_OLD_CLIENT()`.
   * `NEW L30-42`: mensagens da Árvore de Habilidades: `SKILL_UPGRADED()`, `SKILL_NO_POINTS()`, `SKILL_MAX_TIER()`, `SKILL_LOCKED_LEVEL()`, `SKILL_RESPEC_SUCCESS()`, `SKILL_RESPEC_FAIL_MONEY()`, `SKILL_NOTHING_TO_RESPEC()`.
   * `NEW L91-115`: mensagens e tradução de ferramentas: `TOOL_STORED_LORE()`, `PICKUP_WITH_TOOL()`, `PICKUP_WITHOUT_TOOL()`, e `TOOL_NAME(material: Material?)` que consulta o mapa `tool-names.<MATERIAL>` no `messages.yml` (fallback para nome formatado do enum).
   * `NEW L115`: `GIVE_MAX_LEVEL()` para erro quando o comando `/axminions give` excede o nível máximo configurado para o minion.
3. **`api/.../data/DataHandler.kt`** *(+4L)*
   * `NEW L47`: `fun getUserMenuMode(uuid: UUID): String?`
   * `NEW L49`: `fun setUserMenuMode(uuid: UUID, name: String, mode: String)`
4. **`api/.../integrations/types/PricesIntegration.kt`** *(+3L)*
   * `NEW L11`: adiciona método default `fun getPrice(itemStack: ItemStack, player: OfflinePlayer?): Double = getPrice(itemStack)` permitindo que integrações de economia/loja (como HeroVender) resolvam preço com base nas permissões/lojas do dono do minion mesmo offline.
5. **`api/.../minions/Minion.kt`** *(+41L)*
   * `NEW L31`: overload `fun getAsItem(includeTool: Boolean): ItemStack`.
   * `NEW L33`: `fun pickupByPlayer(player: Player, keepToolInside: Boolean)`.
   * `NEW L126-152`: API completa de Habilidades e Menus:
     * `getSkillsData(): String`, `getSkillAllocations(): Map<String, Int>`
     * `getStatTier(statId: String): Int`, `getAbilityTier(abilityId: String): Int`
     * `getTotalSkillPoints(): Int`, `getSpentSkillPoints(): Int`, `getAvailableSkillPoints(): Int`
     * `upgradeSkill(skillId: String, player: Player): Boolean`, `respecSkills(player: Player): Boolean`
     * `getEffectiveRange(): Double`, `getEffectiveSpeed(): Int`
     * `openChestInventory(player: Player)`, `openSkillsInventory(player: Player)`, `openToolSwapInventory(player: Player)`
   * `NEW L154-160`: API de auditoria de propriedade:
     * `getFirstOwner(): String`, `getOwnershipHistory(): List<String>`, `getOwnershipHistoryString(): String`, `setOwnershipInfo(firstOwner: String, history: List<String>)`.
6. **`api/.../minions/miniontype/MinionType.kt`** *(+49L)*
   * `NEW L75-119` (`getItem`): assinatura ampliada para aceitar `skills: String = ""`, `serializedTool: String = ""`, `toolDisplayName: String? = null`, `firstOwner: String = ""`, `ownerHistory: String = ""`. Grava no `PersistentDataContainer` as chaves `Keys.SKILLS`, `Keys.TOOL`, `Keys.FIRST_OWNER`, `Keys.OWNER_HISTORY` e adiciona a lore formatada `Messages.TOOL_STORED_LORE()` quando há ferramenta embutida.
   * `NEW L171-184`: adiciona cache `@Volatile var cachedSkillSpec: SkillSystemSpec? = null`, `fun getSkillSystemSpec(): SkillSystemSpec` (lazy parse via `SkillConfigParser.parse`) e `fun invalidateSkillCache()`.
7. **`api/.../minions/utils/ChunkPos.kt`** *(L22)*
   * Correção de bug crítico no método de estado de chunk:
     * `HEAD`: `it.setTicking(true)` (ignorava o parâmetro `ticking`, nunca parando o tick!)
     * `NEW L22`: `it.setTicking(ticking)` (propaga corretamente `true` ou `false`).
8. **`api/.../utils/Keys.kt`** *(+8L, NEW L19-26)*
   * Adicionadas `NamespacedKey`: `SKILLS`, `TOOL`, `FIRST_OWNER`, `OWNER_HISTORY`.
9. **`api/.../utils/MinionUtils.kt`** *(+73L)*
   * `NEW L68-76` (`isWoodBlock(material: Material)`): inclui madeiras tradicionais, `*_HYPHAE`, `BAMBOO_BLOCK` e `STRIPPED_BAMBOO_BLOCK`.
   * `NEW L79-86` (`isLeafBlock(material: Material)`): inclui `*_LEAVES`, raízes de mangue (`MANGROVE_ROOTS`/`MUDDY_MANGROVE_ROOTS`) e blocos de fungo (`NETHER_WART_BLOCK`, `WARPED_WART_BLOCK`).
   * `NEW L89-122` (`getTree`): otimizado com `HashSet`/`LinkedHashSet` e retorno antecipado evitando alocações redundantes.
   * `NEW L125-152` (`getTreeLeaves(treeBlocks, maxLeaves = 96)`): nova busca BFS a partir dos blocos do tronco para localizar folhas conectadas até o limite de 96 blocos.
10. **[NOVO] `api/.../menus/MinionDialogSpec.kt`** *(21L)*
    * `NEW L6` (`data class DialogButtonSpec`): `label`, `tooltip`, `width`, `closeAfterClick`, `onClick`.
    * `NEW L14` (`data class MinionDialogSpec`): `title`, `bodyLines`, `iconItem`, `columns`, `actions`, `exitButton`. Estrutura desacoplada da API NMS para que `:common` construa dialogs sem importar classes Mojang/Paper 26.3.
11. **[NOVO] `api/.../skills/SkillModels.kt`** *(140L)*
    * `NEW L3`: `enum class AbilityTrigger { ON_ACTION, ON_PERIODIC }`.
    * `NEW L8`: `data class StatSpec` (`id`, `displayName`, `description`, `material`, `slot`, `maxTier`, `costPerTier`, `modifierPerTier`, `chancePerTier`, `maxRangeCap`, `minSpeedFloor`).
    * `NEW L22`: `data class AbilityConditionSpec` (`targetMaterials`, `targetEntities`, `toolEnchants`, `worlds`, `requireOwnerOnline`).
    * `NEW L30`: `data class AbilityEffectSpec` (`type`, `amount`, `factor`, `chance`, `particle`, `sound`, `commands`, `items`, `executableItem`, `count`).
    * `NEW L46`: `data class CustomAbilitySpec` (`id`, `enabled`, `displayName`, `description`, `material`, `slot`, `minLevel`, `maxTier`, `costPerTier`, `trigger`, `chance`, `chancePerTier`, `cooldownSeconds`, `conditions`, `effects`).
    * `NEW L64-140`: `data class SkillSystemSpec` com `getTotalEarnedPoints(level)` (`L72`), `getCostForNextTier(costs, currentTier)` (`L83`), `getCostUpToTier(costs, targetTier)` (`L89`), `getSpentPoints(allocations)` (`L97`), `serializeAllocations(allocations)` (`L115`, formato `"id:tier;..."`) e `deserializeAllocations(raw)` (`L123`).
12. **[NOVO] `api/.../skills/SkillConfigParser.kt`** *(187L)*
    * `NEW L7` `object SkillConfigParser`:
      * `parse(config: Config): SkillSystemSpec` (`L9`): lê `skill-system.*` e `custom-abilities.*` do YAML de cada minion.
      * `parseStatSpec` (`L47`): extrai parâmetros de stats inclusive `per-tier-bonus`, `max-range`, `min-speed`.
      * `parseAbilitySpec` (`L66`): extrai `min-level`, `cooldown-seconds` (ou `cooldown-ticks / 20.0`), gatilhos, condições e lista de efeitos.
      * `parseConditions` (`L101`), `parseEffect` (`L146`), `toDouble` (`L167`), `toInt` (`L175`), `toStringList` (`L183`).

---

## 2.4 Módulo `:common` — Núcleo, Comandos, H2 Database, Minions e Ticker

1. **`AxMinionsPlugin.kt`** *(diff: 48 linhas alteradas)*
   * `NEW L53-82` (`reloadMessages`): passou a extrair e atualizar os 11 arquivos da pasta `lang/*.yml` empacotados no jar (`ar_SA`, `bn_BD`, `de_DE`, `en_US`, `es_ES`, `fr_FR`, `hi_IN`, `id_ID`, `pt_BR`, `ru_RU`, `zh_CN`) e selecionar `lang/${Config.LANGUAGE()}.yml` ao invés de depender apenas do `messages.yml` raiz.
   * `NEW L103-105`: pré-carregamento da classe `InventoryUpdater` shaded logo no `enable` para prevenir `ClassNotFoundException` em recarregamentos dinâmicos.
   * `NEW L174-182`: agenda sincronização atrasada (100 ticks / 5s após inicialização) para chamar `updateLinkedInventory()` em todos os minions carregados após os mundos/chunks estarem prontos.
   * `NEW L213`: no `disable()`, chama `MinionLogger.shutdown()` para encerrar/descarregar o writer de logs com segurança.
2. **`commands/AxMinionsCommand.kt`** *(diff: +588 linhas, 736L no total)*
   * `give(...)` (`L48-83`): agora valida o nível máximo dinâmico configurado em `upgrades` do `MinionType` e envia `Messages.GIVE_MAX_LEVEL()` caso ultrapasse (`L55-66`).
   * `reload(sender)` (`L94-125`): além de recarregar configs/mensagens, agora itera sobre os tipos invalidando cache de habilidades (`invalidateSkillCache()`), limpa `AbilityEngine.clearCooldowns()`, limpa `MenuRouter.clearCache()` e chama `updateLinkedInventory()` nos minions carregados (`L97-113`).
   * **[NOVO]** `menu(player, mode)` (`L126-132`): `/axminions menu [gui|dialog]` delega para `MenuRouter.setOrToggleMenuMode(...)`.
   * **[NOVO]** `adminGui(player, filterOwner)` (`L270-276`): `/axminions admin [dono]` abre `MinionSkillsGui.openAdminGui(...)`.
   * **[NOVO]** `adminList(sender, ownerFilter, page)` (`L277-304`): paginação no chat com donos, histórico, localização, nível e comando clicável `/axminions tp`.
   * **[NOVO]** `adminInspect(player, x, y, z)` (`L305-337`): inspeciona minion olhado via raytrace ou nas coordenadas, exibindo skills, nível, pontos livres/gastos, ferramenta, baú e donos.
   * **[NOVO]** `adminLogs(sender, limit, filter)` (`L338-352`): consulta `MinionLogger.getRecentLogs`.
   * **[NOVO]** `adminTp(player, x, y, z, world)` (`L353-372`): teleporta ao minion informado.
   * **[NOVO]** Suite completa `/axminions debug`:
     * `debugLink` (`L198`), `debugInfo` (`L249`), `debugAbilities` (`L373`), `debugXp` (`L397`), `debugWood` (`L412`), `debugLogs` (`L432`), `debugSync` (`L444`), `debugSetTool` (`L461`), `debugSetLevel` (`L520`), `debugOpen` (`L548`), `debugCheckChest` (`L583`), `debugSpawnMob` (`L605`), `debugUnlink` (`L627`), `debugBreak` (`L654`), `debugPlace` (`L681`).
3. **`data/H2DataHandler.kt`** *(diff: 94 linhas alteradas)*
   * `NEW L31-34`: substituiu `JdbcDataSource` com modo `async:` por URL Hikari padrão `jdbc:h2:./<dataFolder>/data;WRITE_DELAY=0;FILE_LOCK=FS;DB_CLOSE_ON_EXIT=FALSE` utilizando explicitamente `org.h2.Driver`, eliminando corrupções/locks em Windows/Linux.
   * `NEW L86-108`: comandos `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` na inicialização para migrar bancos existentes sem perda de dados:
     * em `axminions_minions`: `skills_data`, `first_owner`, `owner_history`.
     * em `axminions_users`: `menu_mode`.
   * `NEW L157-190` (`loadMinionsForWorld`) e `L295-347` (`saveMinion`): passam a ler e persistir `skills_data`, `first_owner` e `owner_history`.
   * `NEW L513-537`: implementação de `getUserMenuMode(uuid)` e `setUserMenuMode(uuid, name, mode)` (com `INSERT`/`UPDATE` na tabela `axminions_users`).
   * `NEW L540-549` (`disable`): adiciona guarda `if (::dataSource.isInitialized)` antes de fechar o pool Hikari para evitar crash caso o plugin falhe no boot.
4. **`minions/Minion.kt`** *(diff: +390 linhas)*
   * `NEW L72-75`: construtor ampliado com `skillsData: String = ""`, `firstOwnerInput: String = ""`, `ownerHistoryInput: String = ""`.
   * No `init`: chama `updateLinkedInventory()` para já resolver o baú se o chunk estiver pronto.
   * `NEW L168 & L177`: permite operadores (`event.player.isOp`) interagirem/quebrarem minions.
   * `NEW L217-246` (`pickupByPlayer(player, keepToolInside)`): implementa o recolhimento seguro empacotando ou separando a ferramenta e registrando evento `PICKUP` no `MinionLogger`.
   * `NEW L277-279` (`tick`): aciona `AbilityEngine.processPeriodicCycle(this)` no ciclo do minion.
   * `NEW L424-453` (`updateInventory`): adiciona os itens de GUI configuráveis `menu_switch` e `abilities` além do placeholder `<skill_points>`.
   * `openInventory` foi roteado para `MenuRouter.openMinionMenu(this, player)`, enquanto os inventários clássicos passaram a ter métodos dedicados `openChestInventory`, `openSkillsInventory` e `openToolSwapInventory`.
   * `NEW L741-760` (`getLinkedInventory`): passa a resolver o `Inventory` via lazy-load sempre que o chunk do baú estiver carregado (ao invés de apagar `linkedInventory` quando o tick para temporariamente).
   * `NEW L881` (`damageTool`): consulta `AbilityEngine.shouldSaveDurability(this)` (stat `durability_save`) antes de descontar durabilidade do item.
   * `NEW L1038-1209`: novos métodos `updateLinkedInventory`, `clearLinkedInventory`, cálculo de pontos, `upgradeSkill` (com validações de nível mínimo, max tier, custo em pontos e logs), `respecSkills` (cobrando economia via `Integrations.getEconomyIntegration()`), `getEffectiveRange`, `getEffectiveSpeed`, e getters/setters de `firstOwner`/`ownershipHistory`.
5. **`minions/MinionTicker.kt`** *(+4L, NEW L20-22)*
   * A cada 600 ticks (30s), executa `AbilityEngine.pruneExpiredCooldowns()` evitando crescimento infinito do mapa de cooldowns em memória.
6. **`minions/Minions.kt`** *(diff: 118 linhas alteradas)*
   * Todas as operações e iterações sobre a lista `minions` em `startTicking`, `isTicking`, `stopTicking`, `load` e `remove` passam a ser sincronizadas com `lock.read` / `lock.write` (`ReentrantReadWriteLock`), eliminando `ConcurrentModificationException` durante carregamento de chunks ou comandos admin.

---

## 2.5 Módulo `:common` — Listeners e Proteções

1. **`listeners/ChunkListener.kt`** *(+25L)*
   * `NEW L17-29` (`onChunkLoad`): ao carregar um chunk, verifica todos os minions afetados (seja o chunk do próprio minion ou o chunk onde está seu baú vinculado) e chama `updateLinkedInventory()` para restaurar o vínculo imediatamente.
   * `NEW L37-42` (`onChunkUnload`): limpa a referência de `linkedInventory` quando o chunk do baú é descarregado, evitando manter `BlockState`/`Inventory` obsoleto na memória.
2. **`listeners/LinkingListener.kt`** *(+8L, NEW L60-65)*
   * Passa a registrar categoria `LINK_CHEST` no `MinionLogger.log(...)` sempre que um jogador vincula com sucesso um baú a um minion.
3. **`listeners/MinionDamageListener.kt`** *(96L $\rightarrow$ 308L, diff: +298L)*
   * `NEW L37-96`: introduz `data class RecentKill`, `SLAYER_DROP_KEY`, mapas `killedEntityMinions` e `preAttackStackSizes`, e funções `recordRecentAttack`, `recordPreAttack`, `findRecentKillOwner`.
   * `NEW L100-120` (`onMinionKillEntityEvent`): registra quando um Slayer mata uma entidade e cancela/intercepta XP padrão conforme habilidades.
   * `NEW L122-160` (`onEntityDamage`, `onEntityDamageByEntityEvent`): bloqueia dano em entidades protegidas do EliteMobs via `EliteMobsProtection.isEliteEntity(entity)`, e dispara `MinionKillEntityEvent` quando `health - finalDamage <= 0`.
   * `NEW L163-261` (`onEntityDeath`): calcula XP real considerando stack size (`WildStacker`) + multiplicador `AbilityEngine.getXpMultiplier(minion)`; processa drops via `AbilityEngine.processActionDrops` + `ItemUtils.stripAxMinionsNbt`; se `coleta_direta` (`shouldSendDropsDirectToChest`) estiver ativo, envia os itens direto ao baú do minion e limpa `event.drops`; caso contrário marca com `SLAYER_DROP_KEY` para o minion reconhecer.
   * `NEW L263-296` (`onEntityDropItem`, `onItemSpawn`, `onItemMerge`): propaga `SLAYER_DROP_KEY` em merges de stack e limpa NBT interno ao surgir item no chão.
4. **`listeners/MinionInventoryListener.kt`** *(diff: 379 linhas alteradas, 456L)*
   * `L37-44` (`onInventoryDragEvent`) e `L45-247` (`onInventoryClickEvent`): agora interceptam os novos holders `SkillsGuiHolder`, `ToolSwapHolder`, `PickupGuiHolder` e `AdminMinionsHolder`.
   * Menu principal clássico delega cliques para `MinionActions` (`rotate`, `link`, `upgrade`, `statistics`) e trata novas tags `abilities`, `pickup`, `menu_switch`.
   * Novos handlers privados:
     * `handleSkillsClick` (`L254-282`): evolui stat/ability clicada ou executa `respec_skills`.
     * `handleToolSwapClick` (`L283-310`): equipa ferramenta do inventário do player ou remove do slot central `13`.
     * `handlePickupClick` (`L311-335`): escolhe `pickup_with_tool` vs `pickup_without_tool`.
     * `equipToolFromPlayer` (`L336-378`) e `unequipToolToPlayer` (`L436-455`): registram log `TOOL`.
     * `handleAdminClick` (`L379-435`): paginação, logs, teleporte com clique esquerdo e remoção com Shift + Clique Direito no painel `/axminions admin`.
5. **`listeners/MinionPlaceListener.kt`** *(+40L, NEW L55-82 & L186-203)*
   * Ao posicionar um item de minion no mundo, restaura do `PersistentDataContainer`: `Keys.SKILLS`, `Keys.TOOL` (desserializada), `Keys.FIRST_OWNER` (preservando o primeiro comprador/dono original) e adiciona o jogador atual ao `Keys.OWNER_HISTORY` se ainda não constar na lista; registra categoria `PLACE` no `MinionLogger`.

---

## 2.6 Módulo `:common` — Os 8 MinionTypes e NMS Interface

1. **`CollectorMinionType.kt`** *(160L, diff: 109 linhas)*
   * `run` (`L31-160`): ressincronização segura de inventário; quebra em pedaços (`chunk` de stacks) respeitando limite máximo de stack; filtragem via `AbilityEngine.isVoidTrashFiltered` (skill `void_trash`); overflow automático para EnderChest do dono via `tryOverflowToOwnerEnderChest` quando o baú lota; coleta contínua de orbs de XP (`collectNearbyXpOrbsIfEnabled`); e remoção de NBT interno via `ItemUtils.stripAxMinionsNbt` antes de depositar itens (essencial para compatibilidade com HeroVender).
2. **`CrafterMinionType.kt`** *(335L, diff: 67 linhas)*
   * `run` (`L26-150`): consulta `AbilityEngine.getCrafterAbilityBonus(minion)` para executar múltiplos crafts em lote (`maxBatch`), aplicar chance de saída duplicada (`awardBonusCraftIfProc` em `L151-158`), assar resultado automaticamente (`auto_smelt_craft`), compactar blocos (`compact_blocks`) e reciclar ingrediente (`byproduct_recycle`).
3. **`FarmerMinionType.kt`** *(165L, diff: 19 linhas)*
   * `run` (`L38-165`): utiliza `NMSHandler.get().getDrops(tool, block)` para drops corretos na 26.3 e passa toda colheita por `AbilityEngine.processActionDrops(minion, drops, block = ...)` suportando `enfardadeira`, `auto_composter`, `golden_crop` e `bountiful_harvest`.
4. **`FisherMinionType.kt`** *(157L, diff: 78 linhas)*
   * `run` (`L37-157`): usa `minion.getEffectiveRange()` (inclusive com água em caldeirões dentro do alcance); consulta `AbilityEngine.getFisherAbilityBonus(minion)` para somar `luck`, rolagens duplas, rolls extras de tempestade (`weatherFrenzyRolls`) e rolls extras de custom drops; executa `CustomDropRoller.roll(minion, luck)` integrando drops customizados do `fisher.yml` e passa resultados finais por `AbilityEngine.processActionDrops`.
5. **`LumberMinionType.kt`** *(158L, diff: 123 linhas)*
   * `run` (`L37-158`) + `getSaplingType` (`L138-158`): suporte aos tipos de madeira e bambu da 26.3 (`MinionUtils.isWoodBlock` / `getTreeLeaves`); se `AbilityEngine.shouldClearTreeLeaves` estiver ativo, limpa até 96 folhas conectadas junto ao corte; normaliza mudas para replantio; aplica `processSaplingFertilizer`; roda `CustomDropRoller.roll` para os 8 drops do `lumber.yml` escalados por `getLumberRareDropMultiplier`; e passa drops por `processActionDrops`.
6. **`MinerMinionType.kt`** *(408L, diff: 288 linhas)*
   * `run` (`L67-408`): trava segurança mínima de durabilidade $\ge 15$ para não quebrar ferramentas encantadas valiosas; integra KGenerators 7.3 usando simulação segura de quebra manual com travamento temporário de direção/olhar do dono e verificação WorldGuard antes de quebrar; suporta pulso rápido `isMinerOverdriveTriggered` (`overdrive_pulse`); e centraliza envio via `deliverMinedBlockDrops(minion, block, drops)` (`L391-408`) aplicando `processActionDrops` e `stripAxMinionsNbt`.
7. **`SellerMinionType.kt`** *(144L, diff: 43 linhas)*
   * `run` (`L30-105`) e `sellItemStack` (`L106-144`): passou a inspecionar e vender conteúdo interno de Shulker Boxes dentro do baú (`fromShulker = true`), chamando `getPrice(item, owner)` com o `OfflinePlayer` dono; aplica `AbilityEngine.applySellerSaleAbilities` (multiplicadores, venda de pack $\ge 64$, bônus shulker, crítica, pico de mercado, moeda da sorte e depósito direto no Vault quando `deposito_vault` ativo).
8. **`SlayerMinionType.kt`** *(252L, diff: +234 linhas)*
   * `run` (`L42-140`): permite operar mesmo sem baú vinculado (quando aplicável), detecta spawners do WildStacker em chunk $3 \times 3$ ao redor e aplica `collectNearbyDrops(minion)` (`L141-214`) com agendamento em $+2$ ticks e $+8$ ticks para capturar loot após morte assíncrona de stacks.
   * `attackEntityWithSkills` (`L215-236`): consulta `getSlayerAoeTargets` (`shockwave_cleave`) para acertar alvos extras em área e `shouldFrenzyDoubleStrike` (`executioner_frenzy`) para disparar segundo golpe imediato.
   * `canAttack` (`L237-252`): filtra entidades via `EliteMobsProtection.isEliteEntity`, respeita regras `damage-animals`, `damage-tamed`, `damage-renamed`, protege `Villager` e `Steerable`, ignorando nomes artificiais gerados apenas por regex de stacker.
9. **`nms/NMSHandler.kt`** *(diff: +8 linhas)*
   * Adiciona método abstrato `fun openMinionDialog(player: Player, spec: MinionDialogSpec): Boolean = false` para permitir abertura de Dialogs nativos a partir do `:common`.

---

## 2.7 Módulo `:common` — Novos Subsistemas (Skills, Menus, Logs, Drops, Utils)

1. **[NOVO] `skills/AbilityEngine.kt`** *(1255L)*
   * `L36` `object AbilityEngine`: motor central da árvore de habilidades.
   * `L39-198`: tabelas internas imutáveis de conversão `WOOD_SPECIES`, `SMELT_MAP`, `PLANKS_MAP`, `COMPACT_MAP` (count + bloco), `FOSSIL_POOL`, `GEODE_POOL`, `TRASH_FISHER_INPUTS`, `TRASH_FISHER_TREASURES`, `VOID_DEFAULT_TRASH`, `RARE_ESSENCE_BY_MOB`.
   * `L199-225`: data classes de retorno `ActionOutcome`, `SellerOutcome`, `CrafterBonus`, `FisherBonus`.
   * `L226-235`: `clearCooldowns()`, `pruneExpiredCooldowns()`.
   * `L236-450`: getters e verificadores matemáticos:
     * `getEffectiveRange` (`L236`), `getEffectiveSpeed` (`L265`, inclui `ASSEMBLY_SPEED_REDUCE` até $-50\%$, piso 10 ticks), `getEffectiveStorage` (`L294`), `getEffectiveMultiplier` (`L307`), `getEffectiveLuckBonus` (`L331`), `getXpMultiplier` (`L353`), `shouldSaveDurability` (`L371`, teto $95\%$), `getLumberRareDropMultiplier` (`L385`), `shouldClearTreeLeaves` (`L402`), `getSlayerAoeTargets` (`L415`), `shouldFrenzyDoubleStrike` (`L428`), `isMinerOverdriveTriggered` (`L439`).
   * `L451-517` (`processActionDrops`): processa todos os efeitos `ON_ACTION` (`AUTO_SMELT`, `ORE_COMPACT_SMELT`, `LOGS_TO_PLANKS`, `AUTO_COMPACT`, `MULTIPLY_DROPS`, `CANCEL_TOOL_DAMAGE`, `DIRECT_TO_CHEST`, `MULTI_KILL_STACK`, `GIVE_MONEY`, `GIVE_XP`, `EXECUTABLE_ITEM`, `GIVE_ITEM`, `BONUS_DROP`, `RANDOM_MINERAL_CHEST`, `OBSIDIAN_EXTRA_BREAK`, `BONUS_LOOTING_ROLL`, `MOB_HEAD_DROP`, `MOB_RARE_LOOT`, `FISHER_TRASH_TO_TREASURE`, `FISHER_GOLDEN_BONUS`, `SEEDS_TO_BONEMEAL`, `GOLDEN_CROP_UPGRADE`, `BONUS_CROP_DROP`, `FOREST_SAP_DROP`, `VOID_TRASH`, `CONSOLE_COMMAND`, `PARTICLE`, `SOUND`).
   * `L519-550` (`processPeriodicCycle`): processa efeitos `ON_PERIODIC` (`CHEST_AUTO_COMPRESS`, `MERGE_GROUND_ITEMS`, `CONTINUOUS_VACUUM`, `MOISTURIZE_FARMLAND`, `BONEMEAL_CROP_AURA`, `CROP_EXTRA_GROWTH`, etc.).
   * `L551-815`: `compactChestForCandidates`, `compactWholeLinkedChest`, `isVoidTrashFiltered`, `tryOverflowToOwnerEnderChest`, `collectNearbyXpOrbsIfEnabled`, `processSaplingFertilizer`, `shouldSendDropsDirectToChest`, `getExtraStackKillCount`, `applySellerSaleAbilities`, `getCrafterAbilityBonus`, `getFisherAbilityBonus`.
   * `L1034-1091`: `canTriggerAbility` (checa enabled, tier, minLevel, `matchesConditions`, probabilidade escalonada por tier e cooldown em ms) e `matchesConditions` (online, mundo, material alvo, entidade alvo, encantamento mínimo da ferramenta).
2. **[NOVO] `skills/SkillEffectFormatter.kt`** *(193L)*
   * `formatStatPreviewLines` (`L14-114`) e `formatAbilityEffectLines` (`L115-148`): calculam e exibem nos tooltips da GUI e do Dialog os valores atuais vs. próximo tier e resumo numérico de efeito (`summarizeEffectMagnitude`, `L149`).
3. **[NOVO] `menus/MenuRouter.kt`** *(480L)*
   * Cache thread-safe `playerModeCache` (`ConcurrentHashMap<UUID, String>`).
   * Reflection segura para `FloodgateApi` (`isBedrockPlayer`, `L43`) e `ViaAPI` (`supportsDialogProtocol`, `L55`, checando protocolo $\ge 771$ / 1.21.6).
   * `canUseDialog` (`L69`), `getEffectiveMenuMode` (`L73`), `setOrToggleMenuMode` (`L88`).
   * `openMinionMenu` (`L138`), `openMainDialogOrFallback` (`L147`), `openSkillsDialog` (`L162`), `openPickupDialog` (`L169`) com construtores `buildPickupDialog` (`L176`), `buildMainDialog` (`L226`) e `buildSkillsDialog` (`L358`).
4. **[NOVO] `menus/MinionActions.kt`** *(138L)*
   * Centraliza ações reutilizadas entre Menu Dialog e GUI Clássica: `rotate` (`L19`), `toggleLink` (`L28`), `upgrade` (`L48`, com log `UPGRADE`), `collectStorage` (`L92`), `sendUpgradeFail` (`L113`).
5. **[NOVO] `menus/MinionSkillsGui.kt`** *(440L)*
   * Quatro `InventoryHolder` dedicados: `SkillsGuiHolder` (54s), `ToolSwapHolder` (27s), `PickupGuiHolder` (27s), `AdminMinionsHolder` (54s).
   * `renderBar` (`L43`), `openSkillsGui` (`L53`), `openAdminGui` (`L143`), `openToolSwapGui` (`L224`), `openPickupGui` (`L263`), `buildStatItem` (`L325`), `buildAbilityItem` (`L364`), `makeButton` (`L416`).
6. **[NOVO] `logging/MinionLogger.kt`** *(132L)*
   * Buffer circular em memória (`ConcurrentLinkedDeque`, max 500 linhas) consultável via `getRecentLogs(limit, filter)` (`L99`) + gravador assíncrono em disco por dia (`plugins/AxMinions/logs/YYYY-MM-DD.log`) com `BufferedWriter` próprio e método `shutdown()` (`L120`). Métodos públicos: `log(minion, category, message)` (`L28`) e `logSystem(...)` (`L39`).
7. **[NOVO] `utils/CustomDropRoller.kt`** *(167L)*
   * `roll(minion, effectiveLuck, configPath, maxDrops)` (`L30`): itera sobre seções de `custom-drops`, aplica filtros `enabled`, `min-level`, `min-luck`, soma chance base + `luck-bonus * effectiveLuck`, respeita limite de drops por ciclo (`maxDrops`) e indica se deve substituir o drop vanilla (`replaceVanilla`).
   * `createCustomDropItem` (`L98`): suporta `executable-item` (*ExecutableItems*), materiais Bukkit com `name`, `lore`, `glow`, `enchantments` e executa `commands` do drop com placeholders `<player>` e `<amount>`.
8. **[NOVO] `utils/ItemUtils.kt`** *(135L)*
   * `stripAxMinionsNbt(stack)` (`L35`): limpa tags `axminions:*` do `PersistentDataContainer` e zera `ItemMeta` caso fique efetivamente vazio (`isEffectivelyEmptyMeta`, `L64`), garantindo que itens coletados/minerados empilhem e sejam aceitos por plugins de loja (*HeroVender*).
   * `buildExecutableItem(id, amount, owner)` (`L112`): integração reflexiva segura com `ExecutableItemsManager` (`getInstance().getExecutableItem(id).buildItem(amount, owner)`).

---

## 2.8 Módulo `:common` — Integrações (HeroVender, ItemStacker, WildStacker, EliteMobs)

1. **`integrations/Integrations.kt`** *(+40L)*
   * `NEW L89-97`: registra `ItemStackIntegration` quando plugins `ItemStacker` / `ItemStack` estiverem ativos e configurados.
   * `NEW L108-114`: registra `HeroVenderIntegration` quando `HeroVender` estiver presente.
   * Remoção de bloco duplicado do `ItemsAdder` (anteriormente duplicado nas linhas `L206-211`).
2. **`integrations/stacker/WildStackerIntegration.kt`** *(+22L)*
   * `NEW L16-34` (`calculateStackedExp(entity, baseExp)`): consulta `StackedEntity.getExp(amount, baseExp)` com piso garantido de `baseExp * amount`, corrigindo XP subdimensionado ao matar stacks.
   * `NEW L37` (`getStackSize(item)`): fallback seguro retornando `item.itemStack.amount.toLong()`.
3. **[NOVO] `integrations/prices/HeroVenderIntegration.kt`** *(125L)*
   * `register` (`L22`): localiza reflexivamente a classe principal do HeroVender (`ShopController`) e suas versões de método de preço/venda.
   * `getPrice(itemStack)` (`L55`) e `getPrice(itemStack, player: OfflinePlayer?)` (`L59`): resolve as lojas candidatas para o dono via `resolveCandidateShops` (`L87`, verificando permissões de shop quando online/fallback para loja padrão) e calcula o maior preço válido para item sanitizado sem meta residual do plugin.
4. **[NOVO] `integrations/protection/EliteMobsProtection.kt`** *(63L)*
   * `isEliteEntity(entity)` (`L37`): carrega via reflection `com.magmaguy.elitemobs.entitytracker.EntityTracker` (`isEliteMob(Entity)`) para impedir que Slayers ataquem ou matem bosses/mobs de evento.
5. **[NOVO] `integrations/stacker/ItemStackIntegration.kt`** *(55L)*
   * Implementa `StackerIntegration` para o plugin ItemStacker/ItemStack: `getStackSize(entity)`, `getStackSize(item)` (via API de quantidade stackada no chão), `setStackSize`, `dropItemAt`.

---

## 2.9 Módulo `:nms:v26_3` (DamageHandler, LootHandler, DialogMenuHandler, NMSHandler)

1. **`nms/v26_3/DamageHandler.kt`** *(235L $\rightarrow$ 131L)*
   * Refatoração completa removendo a criação da entidade falsa `net.minecraft.world.entity.animal.fox.Fox` em NMS (que quebrava/bugava eventos e metadados na 26.3).
   * Novo mecanismo baseado em `ThreadLocal<Minion?>` combinando cálculo nativo de atributo de ataque + bônus de encantamentos (`Sharpness`, `Smite` para `isUndead`, `Bane of Arthropods` para `isArthropod`), bypass de i-frame (`invulnerability ticks = 0` antes do dano), limpeza de alvo/aggro no mob atacado e evento Bukkit padrão rastreável pelo `MinionDamageListener`.
2. **`nms/v26_3/LootHandler.kt`** *(diff: 67 linhas alteradas)*
   * Corrige geração de loot de pesca na 26.3 implementando pesos vanilla reais de `Luck of the Sea` (distribuindo entre `BuiltInLootTables.FISHING_JUNK`, `FISHING_TREASURE` e `FISHING_FISH`) envolvidos por bloco `try/catch` com fallback seguro para `ItemStack(Material.COD)` caso a loot table falhe por contexto nulo.
3. **`nms/v26_3/NMSHandler.kt`** *(diff: 58 linhas alteradas)*
   * Implementa `getExp` e `getDrops` compatíveis com blocos/ferramentas da 26.3.
   * Conecta `openMinionDialog(player, spec)` chamando `DialogMenuHandler.openDialog(player, spec)`.
4. **[NOVO] `nms/v26_3/DialogMenuHandler.kt`** *(109L)*
   * Usa a Paper 26.3 Registry/Dialog API (`io.papermc.paper.dialog.Dialog`, `DialogBase`, `DialogType.multiAction(...)`, `ActionButton`, `DialogAction.customClick(...)`, `player.showDialog(...)`).
   * `toComponent` (`L35`), `openDialog` (`L57`), `toActionButton` (`L90`): converte cada `DialogButtonSpec` para botão nativo clicável com callbacks no próprio jogador, ícone opcional e número de colunas configurável.

---

## 2.10 Arquivos YAML e Traduções (`config.yml`, `messages.yml`, `lang/*`, `minions/*`)

1. **`common/src/main/resources/plugin.yml`** *(+3L)*
   * Adiciona `HeroVender` na lista `softdepend`.
2. **`common/src/main/resources/config.yml`** *(diff: 156 linhas)*
   * `language: pt_BR`, `use-dialog-menu: true`, `config-version: 11`.
   * Tradução completa para Português do Brasil e inclusão de hooks `ItemStacker` e `HeroVender`, item alimentício `cooked_beef`, e botões extras `abilities`, `pickup` e `menu_switch` na configuração de GUI.
3. **`common/src/main/resources/messages.yml`** *(diff: 168 linhas)*
   * `config-version: 11`. Traduzido para PT-BR.
   * Cores de nível adicionadas do `11` ao `20`; seção `menu` (troca Dialog/GUI e avisos Bedrock/versão antiga); seção `skills` (upgrade, pontos, nível mínimo, respec); seção `pickup` (`with-tool` / `without-tool`); `errors.give-max-level`; `tools.stored-lore` e mapa completo `tool-names` em Português.
4. **[NOVOS] `common/src/main/resources/lang/*.yml`** *(11 arquivos, 156 linhas cada, `config-version: 11`)*
   * `ar_SA.yml`, `bn_BD.yml`, `de_DE.yml`, `en_US.yml`, `es_ES.yml`, `fr_FR.yml`, `hi_IN.yml`, `id_ID.yml`, `pt_BR.yml`, `ru_RU.yml`, `zh_CN.yml`. Todos sincronizados com as chaves mais novas da versão `11`.
5. **`common/src/main/resources/minions/*.yml`** *(8 arquivos, todos em `config-version: 2`)*

| Arquivo | Linhas (`HEAD` $\rightarrow$ `NEW`) | Resumo das Configurações (Lv 1 $\rightarrow$ Lv 20) |
| :--- | :---: | :--- |
| `collector.yml` | `134L` $\rightarrow$ `424L` | `range 1.5 -> 2.0`, `speed 200 -> 125`, `storage 200 -> 12000`, 4 stats (`speed`, `range`, `durability_save`, `storage`) e 8 habilidades (`L292-423`). |
| `crafter.yml` | `123L` $\rightarrow$ `376L` | `speed 200 -> 105`, `storage 200 -> 12000`, 2 stats (`speed`, `storage`) e 8 habilidades (`L247-375`). |
| `farmer.yml` | `146L` $\rightarrow$ `423L` | `range 1.0 -> 1.5`, `speed 240 -> 140`, `storage 200 -> 12000`, 4 stats (`speed`, `range`, `durability_save`, `storage`) e 8 habilidades (`L294-422`). |
| `fisher.yml` | `133L` $\rightarrow$ `1055L` | `range 2`, `speed 220 -> 145`, `storage 200 -> 12000`, 4 stats (`speed`, `range`, `luck`, `durability_save`), 8 habilidades (`L287-416`), `custom-drops-max-per-catch: 1` (`L417`) e `custom-drops` com 62 entradas (`L433-1054`). |
| `lumber.yml` | `138L` $\rightarrow$ `481L` | `range 1.5 -> 2.0`, `speed 240 -> 150`, `storage 200 -> 12000`, 4 stats (`speed`, `range`, `durability_save`, `storage`), 8 habilidades (`L292-422`), `custom-drops-max-per-catch: 1` (`L423`) e `custom-drops` com 8 entradas (`L425-480`). |
| `miner.yml` | `161L` $\rightarrow$ `464L` | `whitelist` expandida de ~6 para 35 blocos (`L19-55`: Deepslate, Nether, Ancient Debris, Basalt, Blackstone, End Stone, Amethyst, Glowstone, Crying Obsidian); `range 1`, `speed 220 -> 140`, `storage 200 -> 12000`, 4 stats e 8 habilidades (`L334-463`). |
| `seller.yml` | `148L` $\rightarrow$ `416L` | `multiplier 1.0 -> 1.33`, `speed 600 -> 300`, `storage 100k -> 500M`, 3 stats (`speed`, `multiplier`, `storage`) e 8 habilidades (`L284-415`). |
| `slayer.yml` | `144L` $\rightarrow$ `429L` | `range 1.5 -> 2.0`, `speed 220 -> 145`, `storage 800 -> 60000`, 4 stats (`speed`, `range`, `durability_save`, `storage`) e 8 habilidades (`L297-428`). |

---

<div align="center">
  <sub><b>FIM DO DOCUMENTO — AXMINIONS 1.0.50-GS-DEFINITIVA (26.3)</b></sub>
</div>
