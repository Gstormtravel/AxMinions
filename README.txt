================================================================================
  AXMINIONS FORK - VERSAO 1.0.50-GS-DEFINITIVA (MINECRAFT / PAPER 26.3)
  Comparacao Completa com o Repositorio Oficial Artillex-Studios/AxMinions
  Commit Base Oficial (HEAD): 7339580 ("Add 26.3 support")
================================================================================

INDICE
------
  PARTE 1 - RESUMO PARA USUARIO E GUIA PRATICO DE USO
    1.1 O que e este Fork e para que ele serve
    1.2 Principais Novidades (Visao Geral Simples)
    1.3 Como Usar os Novos Sistemas (Passo a Passo)
    1.4 Arvore de Habilidades e Niveis 11 a 20 (Os 8 Minions)
    1.5 Sistema de Drops Customizados (Fisher e Lumber)
    1.6 Traducoes e 11 Idiomas Nativos

  PARTE 2 - RELATORIO TECNICO DETALHADO (CLASSE, FUNCAO E LINHA POR LINHA)
    2.1 Estatisticas Gerais do Git Diff vs. Oficial (7339580)
    2.2 Build System, Gradle, Java 25, Dependencias e .gitignore
    2.3 Modulo :api (Interfaces, Modelos de Habilidades, Menus e Config)
    2.4 Modulo :common - Nucleo, Comandos, H2 Database, Minions e Ticker
    2.5 Modulo :common - Listeners e Protecoes
    2.6 Modulo :common - Os 8 MinionTypes e NMS Interface
    2.7 Modulo :common - Novos Subsistemas (Skills, Menus, Logs, Drops, Utils)
    2.8 Modulo :common - Integracoes (HeroVender, ItemStacker, WildStacker, EliteMobs)
    2.9 Modulo :nms:v26_3 (DamageHandler, LootHandler, DialogMenuHandler, NMSHandler)
    2.10 Arquivos YAML e Traducoes (config.yml, messages.yml, lang/*, minions/*)


================================================================================
PARTE 1 - RESUMO PARA USUARIO E GUIA PRATICO DE USO
================================================================================

1.1 O QUE E ESTE FORK E PARA QUE ELE SERVE
------------------------------------------
Este projeto e uma versao amplamente melhorada, corrigida e expandida do plugin
oficial AxMinions (https://github.com/Artillex-Studios/AxMinions), atualizada
para rodar exclusivamente e de forma nativa no Minecraft/Paper 26.3 com Java 25 e
Kotlin 2.3.21.

Enquanto o AxMinions oficial vai apenas ate o Nivel 10, tem interfaces simples de
bau (GUI), poucas integracoes e alguns bugs conhecidos em servidores modernos
(como falha ao vender itens com tags NBT no HeroVender, perda de dano/drops do
Slayer ou bau desvinculado apos reiniciar chunks), este fork transforma cada minion
em uma maquina de progressao ate o Nivel 20, com Arvore de Habilidades estilo RPG,
64 habilidades unicas (8 por minion), Menu Dialog nativo do Minecraft 1.21.6+/26.3,
drops customizados avançados, sistema de logs para a staff e protecao completa de
economia e entidades customizadas.


1.2 PRINCIPAIS NOVIDADES (VISAO GERAL SIMPLES)
----------------------------------------------
1) Progressao Dobrada: Nivel 1 ate Nivel 20
   - O oficial termina no Nivel 10. Aqui todos os 8 minions vao ate o Nivel 20,
     com novos custos de dinheiro e acoes, maior armazenamento de XP/dinheiro e
     cores evolutivas na armadura de couro ate o Lv 20.
   - Os valores iniciais de velocidade e alcance foram rebalanceados para nao
     deixar minions Lv 1 apelativos demais; o poder verdadeiro agora e conquistado
     evoluindo o minion e investindo pontos na Arvore de Habilidades.

2) Menu Nativo Moderno (Dialog API) + Compatibilidade Automatica
   - Jogadores no Java 1.21.6 ou superior (incluindo 26.3) recebem por padrao
     o novo Painel Nativo Dialog (janela moderna com botoes largos, tooltips e
     icone em destaque).
   - Jogadores no Bedrock (via Floodgate/Geyser) ou em clientes antigos (via
     ViaVersion abaixo da 1.21.6) sao detectados automaticamente e recebem o
     Menu Classico de Bau (GUI), sem travar nem bugar.
   - Cada jogador escolhe seu modo preferido com `/axminions menu` ou pelo botao
     dentro do proprio painel do minion, e a escolha fica salva no banco H2.

3) Arvore de Habilidades (Stats + 64 Poderes Especiais) e Respec
   - A cada nivel do minion, ele ganha pontos de habilidade configuraveis em
     `points-per-level`.
   - Voce pode gastar pontos em Atributos basicos (Velocidade, Alcance,
     Economia de Durabilidade, Armazenamento, Multiplicador de Venda ou Sorte)
     OU desbloquear Habilidades Especiais (8 por minion = 64 no total!).
   - Errou a build? O botao "Resetar Pontos (Respec)" devolve todos os pontos
     gastos mediante uma taxa em dinheiro (padrao: 1.000.000).

4) Recolher Minion COM ou SEM Ferramenta
   - Ao clicar para recolher o minion, abre-se uma confirmacao oferecendo duas
     opcoes claras:
     * COM a ferramenta: a picareta/espada/machado/enxada/vara fica salva em
       NBT dentro do proprio item do minion (mantendo encantamentos e
       durabilidade) para guardar facilmente em baus ou vender.
     * SEM a ferramenta: o minion volta como item limpo e a ferramenta vai
       separada para o inventario do jogador.

5) Drops Customizados para Pescador (Fisher) e Lenhador (Lumber)
   - Fisher recebeu 62 drops customizados (de algas, prismarinho e tridente
     ate itens tematicos, moedas, pergaminhos e bau/caixas de recompensa).
   - Lumber recebeu 8 drops customizados (gravetos, macas, carvao vegetal,
     favo de mel, resina `RESIN_CLUMP`, maca dourada e maca encantada).
   - Suporte a `executable-item` (ExecutableItems), comandos no console, sorte
     extra via Luck of the Sea (Sorte do Mar) e controle de maximo por acao
     (`custom-drops-max-per-catch: 1`).

6) Correcoes Criticas de Servidores Survival / Skyblock
   - Slayer sem bugs: removeu o "Fox" fantasma do NMS que causava erros de
     dano; agora protege entidades do EliteMobs e Villagers/montarias,
     funciona mesmo sem bau vinculado (quando configurado) e coleta drops no
     chao ou envia direto para o bau com a skill `coleta_direta`.
   - WildStacker melhorado: mata multiplos stacks com a skill
     `golpe_wildstacker` e calcula XP real usando `WildStacker.getExp(...)`.
   - HeroVender integrado e corrigido: remove tags internas de NBT do AxMinions
     (`ItemUtils.stripAxMinionsNbt`) para o item voltar a ser vanilla puro e
     ser aceito pelos menus do HeroVender, resolvendo loja por dono offline e
     permissao; Seller ainda vende direto de dentro de Shulker Boxes no bau!
   - KGenerators atualizado para v7.3 (`KGenerators-7.3-bnull.jar`) com respeito
     ao modo de quebra manual/protecoes WorldGuard.
   - Baus em chunks descarregados: agora o plugin re-sincroniza o bau vinculado
     automaticamente quando o chunk carrega, evitando perda silenciosa de link.
   - Seguranca & Banco H2: cada minion grava quem foi o PRIMEIRO DONO e o
     HISTORICO DE DONOS no banco e no NBT do item. Logs diarios em `logs/` e
     comandos de auditoria para a administracao (`/axminions admin`, `list`,
     `inspect`, `logs`, `tp`, `debug`).


1.3 COMO USAR OS NOVOS SISTEMAS (PASSO A PASSO)
----------------------------------------------
A) Para Jogadores:
  1. Coloque o minion no chao com botao direito (ele registra voce no historico
     de donos e abre automaticamente no futuro).
  2. Clique com o botao direito no minion para abrir o Painel:
     - Clique em "Habilidades" (estrela/esmeralda) para evoluir Velocidade,
       Alcance, Durabilidade, Armazenamento ou Habilidades Especiais.
     - Clique em "Ferramenta" para colocar ou tirar a ferramenta em um menu
       dedicado de troca (slot central 13).
     - Clique em "Vincular Bau" e depois clique com o botao esquerdo em um bau
       para conecta-lo ao minion.
     - Clique em "Trocar Menu" ou digite `/axminions menu [gui|dialog]` para
       alternar entre o Menu Dialog Moderno e o Menu Classico de Bau.
     - Clique em "Recolher Minion" e escolha se quer levar a ferramenta junto
       dentro do item ou devolvida separada.

B) Para Administradores:
  - `/axminions give <tipo> [jogador] [nivel]` -> Da um minion (valida o nivel
    maximo configurado no `.yml` do minion, ate Lv 20).
  - `/axminions admin [dono]` -> Abre uma GUI de 54 slots listando todos os
    minions ativos no servidor, mostrando primeiro dono, historico, bau,
    ferramenta, acoes, XP/dinheiro guardado.
    * Clique Esquerdo no item -> Teleporta ate o minion.
    * Shift + Clique Direito -> Remove o minion do mundo/banco.
  - `/axminions list [dono] [pagina]` -> Lista os minions em texto no chat com
    teleporte clicavel.
  - `/axminions inspect [x y z]` -> Inspeciona detalhadamente o minion que voce
    esta olhando (ou nas coordenadas informadas): nivel, pontos, builds, bau,
    dono original e historico.
  - `/axminions logs [quantidade] [filtro]` -> Mostra os logs recentes em memoria
    (placement, pickup, link, tool, admin, upgrade, etc.).
  - `/axminions tp <x> <y> <z> [mundo]` -> Teleporta ate um minion.
  - `/axminions debug <subcomando>` -> Ferramentas profundas de diagnostico:
    `link`, `info`, `abilities`, `xp`, `wood`, `logs`, `sync`, `settool`,
    `setlevel`, `open`, `checkchest`, `spawnmob`, `unlink`, `break`, `place`.
  - `/axminions reload` -> Recarrega configs, idioma (`lang/*.yml`), caches de
    habilidades, cooldowns do `AbilityEngine`, cache de `MenuRouter` e forca a
    ressincronizacao de baus vinculados.


1.4 ARVORE DE HABILIDADES E NIVEIS 11 A 20 (OS 8 MINIONS)
---------------------------------------------------------
Cada arquivo em `common/src/main/resources/minions/*.yml` agora possui:
  1) `upgrades` do nivel `1` ao `20` (com requisitos de `money` e `actions`, alem
     de `speed`, `range`, `storage`, `multiplier` para o Seller, e visual da
     armadura evoluindo ate o Lv 20).
  2) `skill-system`: define `enabled: true`, `respec-cost-money: 1000000`, tabela
     de pontos ganhos por nivel (`points-per-level`) e `stats` evoluiveis:
     * `speed` (Redstone, slot 10, max-tier 6)
     * `range` (slot 11, max-tier 4; exceto Crafter/Seller que nao usam range)
     * `durability_save` (Anvil, slot 12, max-tier 4; exceto Crafter/Seller)
     * `storage` (Experience Bottle / Gold Block, slot 13 ou 11/12, max-tier 5)
     * Exclusivos: Seller tem `multiplier` (Emerald, slot 11, max-tier 4);
       Fisher tem `luck` (Rabbit Foot, slot 12, max-tier 4).
  3) `custom-abilities`: 8 poderes unicos por minion (slots 19, 21, 23, 25, 28,
     30, 32, 34 da GUI de Habilidades), desbloqueados por nivel minimo
     (`min-level` entre 8 e 20):

  [COLLECTOR - Coletor]
    - `aspirador_xp` (Lv 10, ON_ACTION): coleta orbs de XP proximos.
    - `xp_vacuum` (Lv 11, ON_PERIODIC): aspiracao continua de XP por ciclo.
    - `void_trash` (Lv 13, ON_ACTION): apaga itens lixo configurados (podres,
      osseos, linha, sementes etc.).
    - `compactador_coleta` (Lv 15, ON_ACTION): compacta drops em blocos.
    - `compact_chest` (Lv 15, ON_PERIODIC): comprime todo o bau vinculado.
    - `overflow_teleporter` (Lv 16, ON_ACTION): se o bau encher, tenta enviar
      para o EnderChest do dono.
    - `stack_merger` (Lv 18, ON_PERIODIC): junta stacks soltos no chao para
      reduzir lag.
    - `dimensional_pull` (Lv 19, ON_ACTION): amplia o raio dimensional de
      coleta (`COLLECTOR_RANGE_BOOST`).

  [CRAFTER - Crafteiro]
    - `forja_lote` (Lv 8, ON_ACTION): crafta varios itens em lote por ciclo.
    - `multi_output` (Lv 11, ON_ACTION): chance de saida duplicada (2x).
    - `auto_smelt_craft` (Lv 12, ON_ACTION): assa automaticamente outputs
      craftaveis.
    - `compact_blocks` (Lv 14, ON_ACTION): compacta resultados automaticamente.
    - `obra_prima` (Lv 15, ON_ACTION): chance maior de craft bonus mestre (2x).
    - `byproduct_recycle` (Lv 16, ON_ACTION): devolve ingrediente ao concluir.
    - `master_artisan` (Lv 18, ON_ACTION): reduz intervalo entre acoes
      (`ASSEMBLY_SPEED_REDUCE`).
    - `assembly_line` (Lv 20, ON_ACTION): concede XP extra por linha de montagem.

  [FARMER - Agricultor]
    - `enfardadeira` (Lv 10, ON_ACTION): compacta colheitas (ex: trigo->fardo).
    - `auto_replant_boost` (Lv 11, ON_PERIODIC): acelera estagio de crescimento
      das plantacoes proximas.
    - `hydration_aura` (Lv 13, ON_PERIODIC): umidifica terra arada ao redor.
    - `bone_meal_mist` (Lv 14, ON_PERIODIC): aplica efeito de farinha de osso em
      area periodicamente.
    - `safra_dupla` (Lv 16, ON_ACTION): duplica a colheita (`factor: 2.0`).
    - `auto_composter` (Lv 16, ON_ACTION): converte sementes excedentes em
      bone meal.
    - `golden_crop` (Lv 18, ON_ACTION): chance de transformar cenoura/maca em
      versao dourada (`GOLDEN_CROP_UPGRADE`).
    - `bountiful_harvest` (Lv 20, ON_ACTION): adiciona cultivo bonus extra.

  [FISHER - Pescador]
    - `rede_dupla` (Lv 12, ON_ACTION): multiplica os itens pescados (2x).
    - `deep_bait` (Lv 12, ON_ACTION): transforma lixo da pesca em tesouro.
    - `abyssal_chest` (Lv 14, ON_ACTION): aumenta Sorte do Mar efetiva na pesca.
    - `xp_tide` (Lv 15, ON_ACTION): multiplica XP obtido (1.35x por tier).
    - `instinto_abissal` (Lv 16, ON_ACTION): aumenta quantidade de rolls em
      `custom-drops` por pescada.
    - `storm_fisher` (Lv 17, ON_ACTION): rola pescadas extras quando esta
      chovendo/tempestade no mundo.
    - `kraken_hook` (Lv 18, ON_ACTION): rola bonus dourado exclusivo do pescador.
    - `golden_school` (Lv 19, ON_ACTION): cardume dourado triplicando drops (3x).

  [LUMBER - Lenhador]
    - `fertilidade_muda` (Lv 8, ON_ACTION): acelera crescimento da muda replantada.
    - `sap_collector` (Lv 11, ON_ACTION): chance de extrair resinas/seivas
      (`FOREST_SAP_DROP`).
    - `leaf_decay_instant` (Lv 12, ON_ACTION): limpa folhas conectadas a arvore
      automaticamente (`CLEAR_TREE_LEAVES`).
    - `carvoaria_portatil` (Lv 14, ON_ACTION): assa toras em carvao vegetal.
    - `auto_charcoal` (Lv 15, ON_ACTION): segundo estagio de queima para carvao.
    - `deep_root` (Lv 17, ON_ACTION): aumenta multiplicador de drops raros de
      arvores (`LUMBER_RARE_DROP_BOOST`).
    - `plank_sawmill` (Lv 18, ON_ACTION): converte toras diretamente em tabuas.
    - `ancient_timber` (Lv 20, ON_ACTION): duplica madeira colhida (2x).

  [MINER - Minerador]
    - `fornalha_abissal` (Lv 8, ON_ACTION): auto-smelt em minerios brutos.
    - `xp_magnet` (Lv 11, ON_ACTION): aumenta XP ganho na mineracao (1.25x base).
    - `fossil_finder` (Lv 13, ON_ACTION): encontra fosseis/itens raros do pool
      de mineracao (`BONUS_DROP`).
    - `compact_ore` (Lv 14, ON_ACTION): compacta minerios em blocos no ato.
    - `compactador_mineral` (Lv 15, ON_ACTION): smelt + compactacao direta.
    - `obsidian_breaker` (Lv 16, ON_ACTION): drop extra de Obsidian / Crying
      Obsidian sem gastar durabilidade.
    - `treasure_geode` (Lv 18, ON_ACTION): encontra geodos/tesouros minerais
      (`RANDOM_MINERAL_CHEST`).
    - `overdrive_pulse` (Lv 20, ON_ACTION): chance de pulso eletrico quebrando
      multiplos blocos em sequencia sem cooldown completo.

  [SELLER - Vendedor]
    - `tax_evader` (Lv 11, ON_ACTION): +6% por tier no valor vendido.
    - `deposito_vault` (Lv 12, ON_ACTION): envia dinheiro direto para a conta do
      jogador no Vault em vez de acumular no storage do minion.
    - `bulk_contract` (Lv 13, ON_ACTION): bonus se vender packs de 64+ itens.
    - `vip_broker` (Lv 15, ON_ACTION): eleva o multiplicador base de venda.
    - `venda_critica` (Lv 16, ON_ACTION): chance de venda critica (1.25x).
    - `shulker_liquidation` (Lv 16, ON_ACTION): bonus percentual quando os itens
      vem de dentro de uma Shulker Box no bau.
    - `lucky_coin` (Lv 18, ON_ACTION): chance de moeda da sorte (+500$ por tier).
    - `market_surge` (Lv 20, ON_ACTION): pico de mercado aumentando em +35% por
      tier no momento do proc.

  [SLAYER - Cacador]
    - `coleta_direta` (Lv 10, ON_ACTION): envia os drops do mob morto direto para
      o bau do minion, evitando ficarem perdidos no chao.
    - `looting_edge` (Lv 11, ON_ACTION): rola drops extras simulando Looting.
    - `soul_drain` (Lv 13, ON_ACTION): aumenta o XP drenado dos mobs (1.28x).
    - `head_hunter` (Lv 15, ON_ACTION): chance de derrubar a cabeca do mob morto.
    - `golpe_wildstacker` (Lv 16, ON_ACTION): abate +1 stack adicional por tier em
      mobs empilhados (WildStacker).
    - `shockwave_cleave` (Lv 17, ON_ACTION): atinge +2 alvos extras ao redor.
    - `rare_essence` (Lv 19, ON_ACTION): chance de gerar essencias raras por tipo
      de mob.
    - `executioner_frenzy` (Lv 20, ON_ACTION): chance de golpe duplo imediato.


1.5 SISTEMA DE DROPS CUSTOMIZADOS (FISHER E LUMBER)
---------------------------------------------------
No arquivo do minion (`fisher.yml` com 62 entradas e `lumber.yml` com 8 entradas),
cada drop aceita:
  - `enabled`: true/false
  - `chance`: chance percentual base (ex: 6.0 = 6%, 0.025 = 0.025%)
  - `luck-bonus`: bonus % extra somado por ponto de Sorte/Luck of the Sea
  - `min-level` / `min-luck`: exigencia minima de nivel do minion e sorte
  - `min-amount` / `max-amount`: faixa aleatoria de quantidade
  - `replace-vanilla`: se true, substitui o drop padrao da acao
  - `executable-item`: ID exato de item do plugin ExecutableItems (suporta
    placeholder `<player>`)
  - `material`, `name`, `lore`, `glow`, `enchantments`: para itens Bukkit
  - `commands`: comandos de console suportando `<player>` e `<amount>`
E `custom-drops-max-per-catch: 1` limita quantos custom drops podem sair por
unica acao (somado ao bonus da skill `instinto_abissal` no Pescador).


1.6 TRADUCOES E 11 IDIOMAS NATIVOS
----------------------------------
O `config.yml` agora define `language: pt_BR` como padrao e `config-version: 11`.
Na pasta `common/src/main/resources/lang/`, todos os 11 arquivos de idioma (156
linhas cada, `config-version: 11`) sao extraidos na pasta do plugin ao iniciar:
  `ar_SA.yml`, `bn_BD.yml`, `de_DE.yml`, `en_US.yml`, `es_ES.yml`, `fr_FR.yml`,
  `hi_IN.yml`, `id_ID.yml`, `pt_BR.yml`, `ru_RU.yml`, `zh_CN.yml`.


================================================================================
PARTE 2 - RELATORIO TECNICO DETALHADO (CLASSE, FUNCAO E LINHA POR LINHA)
================================================================================

2.1 ESTATISTICAS GERAIS DO GIT DIFF VS. OFICIAL (7339580)
---------------------------------------------------------
Baseline oficial: `Artillex-Studios/AxMinions`, commit `7339580` ("Add 26.3 support").
Versao do fork: `1.0.50-GS-DEFINITIVA` (Minecraft/Paper `26.3`, Java 25, Kotlin 2.3.21).
Total de mudancas rastreadas em `git diff --stat`:
  - 53 arquivos modificados/deletados rastreados: `6592 insertions(+)`, `1558 deletions(-)`
  - 2 arquivos removidos vs. upstream: `.build.gradle.swp` (arquivo temporario Vim que
    havia vazado no repo oficial) e `libs/KGenerators-7.3-b161.jar`
  - 27 itens novos nao rastreados (12 classes/arquivos Kotlin em `api/`, `common/`,
    `nms/v26_3/`, 11 arquivos YAML em `lang/`, `libs/KGenerators-7.3-bnull.jar`, e este
    `README.txt`).

Nota sobre convencao de linhas neste relatorio:
  - Formato `HEAD Lx -> NEW Ly`: mostra a localizacao no arquivo oficial e a nova
    localizacao no fork apos as modificacoes.
  - Arquivos novos possuem apenas as linhas atuais no fork.


--------------------------------------------------------------------------------
2.2 BUILD SYSTEM, GRADLE, JAVA 25, DEPENDENCIAS E .GITIGNORE
--------------------------------------------------------------------------------

[1] build.gradle (diff: 131 linhas alteradas)
  - Remocao de repositorios duplicados no topo (mavenLocal/central/sonatype duplicados)
    e blocos multi-versao de NMS antigos (`1.20.1` ate `26.2`).
  - Versao alterada para `'1.0.50-GS-DEFINITIVA'`.
  - Remocao de dependencia de plugin `io.github.goooler.shadow` / config antiga de `reobf`
    para subprojetos antigos; configurado `jvmToolchain(25)` para compilar nativamente em
    Java 25 / Kotlin `2.3.21` visando apenas `:api`, `:common`, `:nms:v26_3`.
  - Remocao de `relocate("org.h2", ...)` do `shadowJar` para manter `org.h2.Driver` no
    classpath compativel com o HikariDataSource sem falhas de reflexao/ServiceLoader.

[2] settings.gradle (diff: 27 linhas alteradas)
  - Removido bloco `pluginManagement` com `foojay-resolver-convention` e removidos todos
    os includes antigos (`nms:v1_20_R1`, `R2`, `R3`, `v1_20_R4`, `v1_21`, `v1_21_R2`,
    `v1_21_R3`, `v1_21_R4`, `v1_21_R5`, `v1_21_R6`, `v1_21_R7`, `v26_1`, `v26_2`).
  - Mantidos apenas: `api`, `common`, `nms`, `nms:v26_3`.

[3] gradle.properties (diff: +26 linhas)
  - Adicionados parametros de performance e build em Java 25:
    `org.gradle.jvmargs=-Xmx4G`, `org.gradle.daemon=true`, `org.gradle.parallel=true`,
    `org.gradle.caching=true`, `org.gradle.configuration-cache=true`,
    `kotlin.daemon.jvmargs=-Xmx2G`, `kotlin.incremental=true`,
    `org.gradle.warning.mode=summary`.

[4] api/build.gradle (diff: 2 linhas)
  - Atualizada dependencia compileOnly `org.spigotmc:spigot-api` de `1.20.1-R0.1-SNAPSHOT`
    para `1.20.6-R0.1-SNAPSHOT` para suportar APIs de meta/enchantments modernos.

[5] common/build.gradle (diff: 15 linhas, L1-50)
  - L12: substituido `spigot-api:1.20.1-R0.1-SNAPSHOT` por `1.20.6-R0.1-SNAPSHOT`.
  - L23: substituida dependencia local `../libs/KGenerators-7.3-b161.jar` por
    `compileOnly files('../libs/KGenerators-7.3-bnull.jar')`.
  - L27: adicionado explicitamente `implementation 'com.h2database:h2:2.2.220'`.
  - L36-46: capturado `def pluginVersion = version.toString()` fora da closure do
    `processResources { filesMatching(...) { expand(version: pluginVersion) } }` para
    compatibilidade total com o Configuration Cache do Gradle.

[6] .gitignore (diff: +7 linhas, NEW L38-47)
  - Adicionadas regras `.kotlin/`, `*.swp`, `logs/` antes de `.DS_Store` para manter caches
    do Kotlin, swaps de editor e logs de execucao fora do versionamento Git.

[7] libs/
  - Deletado: `libs/KGenerators-7.3-b161.jar`
  - Adicionado: `libs/KGenerators-7.3-bnull.jar` (mantido rastreado por ser dependencia local
    obrigatoria de compilacao do `common/build.gradle:23`).


--------------------------------------------------------------------------------
2.3 MODULO :api (INTERFACES, MODELOS DE HABILIDADES, MENUS E CONFIG)
--------------------------------------------------------------------------------

[1] api/src/main/kotlin/com/artillexstudios/axminions/api/config/Config.kt (+4L)
  - NEW L20: `fun LANGUAGE() = config.getString("language", "pt_BR")` -> novo seletor
    de idioma com padrao `pt_BR`.
  - NEW L28: `fun USE_DIALOG_MENU() = config.getBoolean("use-dialog-menu", true)` -> chave
    mestra do sistema de Painel Dialog nativo.

[2] api/src/main/kotlin/com/artillexstudios/axminions/api/config/Messages.kt (+46L)
  - NEW L24-28: `MENU_MODE_SET()`, `MENU_MODE_BEDROCK()`, `MENU_MODE_OLD_CLIENT()`.
  - NEW L30-42: mensagens da Arvore de Habilidades: `SKILL_UPGRADED()`, `SKILL_NO_POINTS()`,
    `SKILL_MAX_TIER()`, `SKILL_LOCKED_LEVEL()`, `SKILL_RESPEC_SUCCESS()`,
    `SKILL_RESPEC_FAIL_MONEY()`, `SKILL_NOTHING_TO_RESPEC()`.
  - NEW L91-115: mensagens e traducao de ferramentas: `TOOL_STORED_LORE()`,
    `PICKUP_WITH_TOOL()`, `PICKUP_WITHOUT_TOOL()`, e `TOOL_NAME(material: Material?)`
    que consulta o mapa `tool-names.<MATERIAL>` no `messages.yml` (fallback para nome
    formatado do enum).
  - NEW L115: `GIVE_MAX_LEVEL()` para erro quando o comando `/axminions give` excede o
    nivel maximo configurado para o minion.

[3] api/src/main/kotlin/com/artillexstudios/axminions/api/data/DataHandler.kt (+4L)
  - NEW L47: `fun getUserMenuMode(uuid: UUID): String?`
  - NEW L49: `fun setUserMenuMode(uuid: UUID, name: String, mode: String)`

[4] api/src/main/kotlin/com/artillexstudios/axminions/api/integrations/types/PricesIntegration.kt (+3L)
  - NEW L11: adiciona metodo default `fun getPrice(itemStack: ItemStack, player: OfflinePlayer?): Double = getPrice(itemStack)`
    permitindo que integracoes de economia/loja (como HeroVender) resolvam preco com
    base nas permissoes/lojas do dono do minion mesmo offline.

[5] api/src/main/kotlin/com/artillexstudios/axminions/api/minions/Minion.kt (+41L)
  - NEW L31: overload `fun getAsItem(includeTool: Boolean): ItemStack`.
  - NEW L33: `fun pickupByPlayer(player: Player, keepToolInside: Boolean)`.
  - NEW L126-152: API completa de Habilidades e Menus:
    * `getSkillsData(): String`, `getSkillAllocations(): Map<String, Int>`
    * `getStatTier(statId: String): Int`, `getAbilityTier(abilityId: String): Int`
    * `getTotalSkillPoints(): Int`, `getSpentSkillPoints(): Int`, `getAvailableSkillPoints(): Int`
    * `upgradeSkill(skillId: String, player: Player): Boolean`, `respecSkills(player: Player): Boolean`
    * `getEffectiveRange(): Double`, `getEffectiveSpeed(): Int`
    * `openChestInventory(player: Player)`, `openSkillsInventory(player: Player)`,
      `openToolSwapInventory(player: Player)`
  - NEW L154-160: API de auditoria de propriedade:
    * `getFirstOwner(): String`, `getOwnershipHistory(): List<String>`,
      `getOwnershipHistoryString(): String`, `setOwnershipInfo(firstOwner: String, history: List<String>)`.

[6] api/src/main/kotlin/com/artillexstudios/axminions/api/minions/miniontype/MinionType.kt (+49L/-X)
  - NEW L75-119 (`getItem`): assinatura ampliada para aceitar `skills: String = ""`,
    `serializedTool: String = ""`, `toolDisplayName: String? = null`,
    `firstOwner: String = ""`, `ownerHistory: String = ""`. Grava no `PersistentDataContainer`
    as chaves `Keys.SKILLS`, `Keys.TOOL`, `Keys.FIRST_OWNER`, `Keys.OWNER_HISTORY` e adiciona
    a lore formatada `Messages.TOOL_STORED_LORE()` quando ha ferramenta embutida.
  - NEW L171-184: adiciona cache `@Volatile var cachedSkillSpec: SkillSystemSpec? = null`,
    `fun getSkillSystemSpec(): SkillSystemSpec` (lazy parse via `SkillConfigParser.parse`) e
    `fun invalidateSkillCache()`.

[7] api/src/main/kotlin/com/artillexstudios/axminions/api/minions/utils/ChunkPos.kt (L22)
  - Correcao de bug critico no metodo de estado de chunk:
    HEAD: `it.setTicking(true)` (ignorava o parametro `ticking`, nunca parando o tick!)
    NEW L22: `it.setTicking(ticking)` (propaga corretamente `true` ou `false`).

[8] api/src/main/kotlin/com/artillexstudios/axminions/api/utils/Keys.kt (+8L, NEW L19-26)
  - Adicionadas `NamespacedKey`: `SKILLS`, `TOOL`, `FIRST_OWNER`, `OWNER_HISTORY`.

[9] api/src/main/kotlin/com/artillexstudios/axminions/api/utils/MinionUtils.kt (+73L)
  - NEW L68-76 (`isWoodBlock(material: Material)`): inclui madeiras tradicionais,
    `*_HYPHAE`, `BAMBOO_BLOCK` e `STRIPPED_BAMBOO_BLOCK`.
  - NEW L79-86 (`isLeafBlock(material: Material)`): inclui `*_LEAVES`, raizes de mangue
    (`MANGROVE_ROOTS`/`MUDDY_MANGROVE_ROOTS`) e blocos de fungo (`NETHER_WART_BLOCK`, `WARPED_WART_BLOCK`).
  - NEW L89-122 (`getTree`): otimizado com `HashSet`/`LinkedHashSet` e retorno antecipado
    evitando alocacoes redundantes.
  - NEW L125-152 (`getTreeLeaves(treeBlocks, maxLeaves = 96)`): nova busca BFS a partir dos
    blocos do tronco para localizar folhas conectadas ate o limite de 96 blocos.

[10] [NOVO] api/src/main/kotlin/com/artillexstudios/axminions/api/menus/MinionDialogSpec.kt (21L)
  - NEW L6 (`data class DialogButtonSpec`): `label`, `tooltip`, `width`, `closeAfterClick`, `onClick`.
  - NEW L14 (`data class MinionDialogSpec`): `title`, `bodyLines`, `iconItem`, `columns`,
    `actions`, `exitButton`. Estrutura desacoplada da API NMS para que `:common` construa
    dialogs sem importar classes Mojang/Paper 26.3.

[11] [NOVO] api/src/main/kotlin/com/artillexstudios/axminions/api/skills/SkillModels.kt (140L)
  - NEW L3: `enum class AbilityTrigger { ON_ACTION, ON_PERIODIC }`.
  - NEW L8: `data class StatSpec` (`id`, `displayName`, `description`, `material`, `slot`,
    `maxTier`, `costPerTier`, `modifierPerTier`, `chancePerTier`, `maxRangeCap`, `minSpeedFloor`).
  - NEW L22: `data class AbilityConditionSpec` (`targetMaterials`, `targetEntities`,
    `toolEnchants`, `worlds`, `requireOwnerOnline`).
  - NEW L30: `data class AbilityEffectSpec` (`type`, `amount`, `factor`, `chance`, `particle`,
    `sound`, `commands`, `items`, `executableItem`, `count`).
  - NEW L46: `data class CustomAbilitySpec` (`id`, `enabled`, `displayName`, `description`,
    `material`, `slot`, `minLevel`, `maxTier`, `costPerTier`, `trigger`, `chance`,
    `chancePerTier`, `cooldownSeconds`, `conditions`, `effects`).
  - NEW L64-140: `data class SkillSystemSpec` com `getTotalEarnedPoints(level)` (L72),
    `getCostForNextTier(costs, currentTier)` (L83), `getCostUpToTier(costs, targetTier)` (L89),
    `getSpentPoints(allocations)` (L97), `serializeAllocations(allocations)` (L115,
    formato `"id:tier;..."`) e `deserializeAllocations(raw)` (L123).

[12] [NOVO] api/src/main/kotlin/com/artillexstudios/axminions/api/skills/SkillConfigParser.kt (187L)
  - NEW L7 `object SkillConfigParser`:
    * `parse(config: Config): SkillSystemSpec` (L9): le `skill-system.*` e `custom-abilities.*`
      do YAML de cada minion.
    * `parseStatSpec` (L47): extrai parametros de stats inclusive `per-tier-bonus`, `max-range`,
      `min-speed`.
    * `parseAbilitySpec` (L66): extrai `min-level`, `cooldown-seconds` (ou `cooldown-ticks / 20.0`),
      gatilhos, condicoes e lista de efeitos.
    * `parseConditions` (L101), `parseEffect` (L146), `toDouble` (L167), `toInt` (L175),
      `toStringList` (L183).


--------------------------------------------------------------------------------
2.4 MODULO :common - NUCLEO, COMANDOS, H2 DATABASE, MINIONS E TICKER
--------------------------------------------------------------------------------

[1] AxMinionsPlugin.kt (diff: 48 linhas alteradas)
  - NEW L53-82 (`reloadMessages`): passou a extrair e atualizar os 11 arquivos da pasta
    `lang/*.yml` empacotados no jar (`ar_SA`, `bn_BD`, `de_DE`, `en_US`, `es_ES`, `fr_FR`,
    `hi_IN`, `id_ID`, `pt_BR`, `ru_RU`, `zh_CN`) e selecionar `lang/${Config.LANGUAGE()}.yml`
    ao inves de depender apenas do `messages.yml` raiz.
  - NEW L103-105: pre-carregamento da classe `InventoryUpdater` shaded logo no `enable` para
    prevenir `ClassNotFoundException` em recarregamentos dinamicos.
  - NEW L174-182: agenda sincronizacao atrasada (100 ticks / 5s apos inicializacao) para chamar
    `updateLinkedInventory()` em todos os minions carregados apos os mundos/chunks estarem
    prontos.
  - NEW L213: no `disable()`, chama `MinionLogger.shutdown()` para encerrar/descarregar o
    writer de logs com seguranca.

[2] commands/AxMinionsCommand.kt (diff: +588 linhas, 736L no total)
  - `give(...)` (L48-83): agora valida o nivel maximo dinamico configurado em `upgrades` do
    `MinionType` e envia `Messages.GIVE_MAX_LEVEL()` caso ultrapasse (L55-66).
  - `reload(sender)` (L94-125): alem de recarregar configs/mensagens, agora itera sobre os
    tipos invalidando cache de habilidades (`invalidateSkillCache()`), limpa
    `AbilityEngine.clearCooldowns()`, limpa `MenuRouter.clearCache()` e chama
    `updateLinkedInventory()` nos minions carregados (L97-113).
  - [NOVO] `menu(player, mode)` (L126-132): `/axminions menu [gui|dialog]` delega para
    `MenuRouter.setOrToggleMenuMode(...)`.
  - [NOVO] `adminGui(player, filterOwner)` (L270-276): `/axminions admin [dono]` abre
    `MinionSkillsGui.openAdminGui(...)`.
  - [NOVO] `adminList(sender, ownerFilter, page)` (L277-304): paginacao no chat com donos,
    historico, localizacao, nivel e comando clicavel `/axminions tp`.
  - [NOVO] `adminInspect(player, x, y, z)` (L305-337): inspeciona minion olhado via raytrace
    ou nas coordenadas, exibindo skills, nivel, pontos livres/gastos, ferramenta, bau e donos.
  - [NOVO] `adminLogs(sender, limit, filter)` (L338-352): consulta `MinionLogger.getRecentLogs`.
  - [NOVO] `adminTp(player, x, y, z, world)` (L353-372): teleporta ao minion informado.
  - [NOVO] Suite completa `/axminions debug`:
    * `debugLink` (L198): vincula manualmente um bau ao minion nas coordenadas.
    * `debugInfo` (L249): estado interno de tick, chunk e inventarios.
    * `debugAbilities` (L373): lista stats, tiers e abilities ativas/desbloqueadas do minion.
    * `debugXp` (L397), `debugWood` (L412), `debugLogs` (L432), `debugSync` (L444).
    * `debugSetTool` (L461), `debugSetLevel` (L520), `debugOpen` (L548).
    * `debugCheckChest` (L583), `debugSpawnMob` (L605), `debugUnlink` (L627).
    * `debugBreak` (L654), `debugPlace` (L681).

[3] data/H2DataHandler.kt (diff: 94 linhas alteradas)
  - NEW L31-34: substituiu `JdbcDataSource` com modo `async:` por URL Hikari padrao
    `jdbc:h2:./<dataFolder>/data;WRITE_DELAY=0;FILE_LOCK=FS;DB_CLOSE_ON_EXIT=FALSE`
    utilizando explicitamente `org.h2.Driver`, eliminando corrupcoes/locks em Windows/Linux.
  - NEW L86-108: comandos `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` na inicializacao para
    migrar bancos existentes sem perda de dados:
    * em `axminions_minions`: `skills_data`, `first_owner`, `owner_history`.
    * em `axminions_users`: `menu_mode`.
  - NEW L157-190 (`loadMinionsForWorld`) e L295-347 (`saveMinion`): passam a ler e persistir
    `skills_data`, `first_owner` e `owner_history`.
  - NEW L513-537: implementacao de `getUserMenuMode(uuid)` e `setUserMenuMode(uuid, name, mode)`
    (com INSERT/UPDATE na tabela `axminions_users`).
  - NEW L540-549 (`disable`): adiciona guarda `if (::dataSource.isInitialized)` antes de fechar
    o pool Hikari para evitar crash caso o plugin falhe no boot.

[4] minions/Minion.kt (diff: +390 linhas)
  - NEW L72-75: construtor ampliado com `skillsData: String = ""`, `firstOwnerInput: String = ""`,
    `ownerHistoryInput: String = ""`.
  - No `init`: chama `updateLinkedInventory()` para ja resolver o bau se o chunk estiver pronto.
  - NEW L168 & L177: permite operadores (`event.player.isOp`) interagirem/quebrarem minions.
  - NEW L217-246 (`pickupByPlayer(player, keepToolInside)`): implementa o recolhimento seguro
    empacotando ou separando a ferramenta e registrando evento `PICKUP` no `MinionLogger`.
  - NEW L277-279 (`tick`): aciona `AbilityEngine.processPeriodicCycle(this)` no ciclo do minion.
  - NEW L424-453 (`updateInventory`): adiciona os itens de GUI configuraveis `menu_switch` e
    `abilities` alem do placeholder `<skill_points>`.
  - `openInventory` foi roteado para `MenuRouter.openMinionMenu(this, player)`, enquanto os
    inventarios classic passaram a ter metodos dedicados `openChestInventory`,
    `openSkillsInventory` e `openToolSwapInventory`.
  - NEW L741-760 (`getLinkedInventory`): passa a resolver o `Inventory` via lazy-load sempre
    que o chunk do bau estiver carregado (ao inves de apagar `linkedInventory` quando o tick
    para temporariamente).
  - NEW L881 (`damageTool`): consulta `AbilityEngine.shouldSaveDurability(this)` (stat
    `durability_save`) antes de descontar durabilidade do item.
  - NEW L1038-1209: novos metodos `updateLinkedInventory`, `clearLinkedInventory`, calculo de
    pontos, `upgradeSkill` (com validacoes de nivel minimo, max tier, custo em pontos e logs),
    `respecSkills` (cobrando economia via `Integrations.getEconomyIntegration()`),
    `getEffectiveRange`, `getEffectiveSpeed`, e getters/setters de `firstOwner`/`ownershipHistory`.

[5] minions/MinionTicker.kt (+4L, NEW L20-22)
  - A cada 600 ticks (30s), executa `AbilityEngine.pruneExpiredCooldowns()` evitando crescimento
    infinito do mapa de cooldowns em memoria.

[6] minions/Minions.kt (diff: 118 linhas alteradas)
  - Todas as operacoes e iteracoes sobre a lista `minions` em `startTicking`, `isTicking`,
    `stopTicking`, `load` e `remove` passam a ser sincronizadas com `lock.read` / `lock.write`
    (`ReentrantReadWriteLock`), eliminando `ConcurrentModificationException` durante carregamento
    de chunks ou comandos admin.


--------------------------------------------------------------------------------
2.5 MODULO :common - LISTENERS E PROTECOES
--------------------------------------------------------------------------------

[1] listeners/ChunkListener.kt (+25L)
  - NEW L17-29 (`onChunkLoad`): ao carregar um chunk, verifica todos os minions afetados (seja
    o chunk do proprio minion ou o chunk onde esta seu bau vinculado) e chama
    `updateLinkedInventory()` para restaurar o vinculo imediatamente.
  - NEW L37-42 (`onChunkUnload`): limpa a referencia de `linkedInventory` quando o chunk do
    bau e descarregado, evitando manter `BlockState`/`Inventory` obsoleto na memoria.

[2] listeners/LinkingListener.kt (+8L, NEW L60-65)
  - Passa a registrar categoria `LINK_CHEST` no `MinionLogger.log(...)` sempre que um jogador
    vincula com sucesso um bau a um minion.

[3] listeners/MinionDamageListener.kt (96L -> 308L, diff: +298L)
  - NEW L37-96: introduz `data class RecentKill`, `SLAYER_DROP_KEY`, mapas
    `killedEntityMinions` e `preAttackStackSizes`, e funcoes `recordRecentAttack`,
    `recordPreAttack`, `findRecentKillOwner`.
  - NEW L100-120 (`onMinionKillEntityEvent`): registra quando um Slayer mata uma entidade e
    cancela/intercepta XP padrao conforme habilidades.
  - NEW L122-160 (`onEntityDamage`, `onEntityDamageByEntityEvent`): bloqueia dano em entidades
    protegidas do EliteMobs via `EliteMobsProtection.isEliteEntity(entity)`, e dispara
    `MinionKillEntityEvent` quando `health - finalDamage <= 0`.
  - NEW L163-261 (`onEntityDeath`): calcula XP real considerando stack size (`WildStacker`) +
    multiplicador `AbilityEngine.getXpMultiplier(minion)`; processa drops via
    `AbilityEngine.processActionDrops` + `ItemUtils.stripAxMinionsNbt`; se `coleta_direta`
    (`shouldSendDropsDirectToChest`) estiver ativo, envia os itens direto ao bau do minion e
    limpa `event.drops`; caso contrario marca com `SLAYER_DROP_KEY` para o minion reconhecer.
  - NEW L263-296 (`onEntityDropItem`, `onItemSpawn`, `onItemMerge`): propaga `SLAYER_DROP_KEY`
    em merges de stack e limpa NBT interno ao surgir item no chao.

[4] listeners/MinionInventoryListener.kt (diff: 379 linhas alteradas, 456L)
  - L37-44 (`onInventoryDragEvent`) e L45-247 (`onInventoryClickEvent`): agora interceptam os
    novos holders `SkillsGuiHolder`, `ToolSwapHolder`, `PickupGuiHolder` e `AdminMinionsHolder`.
  - Menu principal classico delega cliques para `MinionActions` (`rotate`, `link`, `upgrade`,
    `statistics`) e trata novas tags `abilities`, `pickup`, `menu_switch`.
  - Novos handlers privados:
    * `handleSkillsClick` (L254-282): evolui stat/ability clicada ou executa `respec_skills`.
    * `handleToolSwapClick` (L283-310): equipa ferramenta do inventario do player ou remove do
      slot central 13.
    * `handlePickupClick` (L311-335): escolhe `pickup_with_tool` vs `pickup_without_tool`.
    * `equipToolFromPlayer` (L336-378) e `unequipToolToPlayer` (L436-455): registram log `TOOL`.
    * `handleAdminClick` (L379-435): paginacao, logs, teleporte com clique esquerdo e remocao
      com Shift + Clique Direito no painel `/axminions admin`.

[5] listeners/MinionPlaceListener.kt (+40L, NEW L55-82 & L186-203)
  - Ao posicionar um item de minion no mundo, restaura do `PersistentDataContainer`:
    `Keys.SKILLS`, `Keys.TOOL` (desserializada), `Keys.FIRST_OWNER` (preservando o primeiro
    comprador/dono original) e adiciona o jogador atual ao `Keys.OWNER_HISTORY` se ainda
    nao constar na lista; registra categoria `PLACE` no `MinionLogger`.


--------------------------------------------------------------------------------
2.6 MODULO :common - OS 8 MINIONTYPES E NMS INTERFACE
--------------------------------------------------------------------------------

[1] CollectorMinionType.kt (160L, diff: 109 linhas)
  - `run` (L31-160): ressincronizacao segura de inventario; quebra em pedacos (`chunk` de stacks)
    respeitando limite maximo de stack; filtragem via `AbilityEngine.isVoidTrashFiltered`
    (skill `void_trash`); overflow automatico para EnderChest do dono via
    `tryOverflowToOwnerEnderChest` quando o bau lota; coleta continua de orbs de XP
    (`collectNearbyXpOrbsIfEnabled`); e remocao de NBT interno via `ItemUtils.stripAxMinionsNbt`
    antes de depositar itens (essencial para compatibilidade com HeroVender).

[2] CrafterMinionType.kt (335L, diff: 67 linhas)
  - `run` (L26-150): consulta `AbilityEngine.getCrafterAbilityBonus(minion)` para executar
    multiplos crafts em lote (`maxBatch`), aplicar chance de saida duplicada (`awardBonusCraftIfProc`
    em L151-158), assar resultado automaticamente (`auto_smelt_craft`), compactar blocos
    (`compact_blocks`) e reciclar ingrediente (`byproduct_recycle`).

[3] FarmerMinionType.kt (165L, diff: 19 linhas)
  - `run` (L38-165): utiliza `NMSHandler.get().getDrops(tool, block)` para drops corretos na
    26.3 e passa toda colheita por `AbilityEngine.processActionDrops(minion, drops, block = ...)`
    suportando `enfardadeira`, `auto_composter`, `golden_crop` e `bountiful_harvest`.

[4] FisherMinionType.kt (157L, diff: 78 linhas)
  - `run` (L37-157): usa `minion.getEffectiveRange()` (inclusive com agua em caldeiroes dentro
    do alcance); consulta `AbilityEngine.getFisherAbilityBonus(minion)` para somar `luck`,
    rolagens duplas, rolls extras de tempestade (`weatherFrenzyRolls`) e rolls extras de
    custom drops; executa `CustomDropRoller.roll(minion, luck)` integrando drops customizados
    do `fisher.yml` e passa resultados finais por `AbilityEngine.processActionDrops`.

[5] LumberMinionType.kt (158L, diff: 123 linhas)
  - `run` (L37-158) + `getSaplingType` (L138-158): suporte aos tipos de madeira e bambu da
    26.3 (`MinionUtils.isWoodBlock` / `getTreeLeaves`); se `AbilityEngine.shouldClearTreeLeaves`
    estiver ativo, limpa ate 96 folhas conectadas junto ao corte; normaliza mudas para replantio;
    aplica `processSaplingFertilizer`; roda `CustomDropRoller.roll` para os 8 drops do
    `lumber.yml` escalados por `getLumberRareDropMultiplier`; e passa drops por `processActionDrops`.

[6] MinerMinionType.kt (408L, diff: 288 linhas)
  - `run` (L67-408): trava seguranca minima de durabilidade >= 15 para nao quebrar ferramentas
    encantadas valiosas; integra KGenerators 7.3 usando simulacao segura de quebra manual com
    travamento temporario de direcao/olhar do dono e verificacao WorldGuard antes de quebrar;
    suporta pulso rapido `isMinerOverdriveTriggered` (`overdrive_pulse`); e centraliza envio
    via `deliverMinedBlockDrops(minion, block, drops)` (L391-408) aplicando `processActionDrops`
    e `stripAxMinionsNbt`.

[7] SellerMinionType.kt (144L, diff: 43 linhas)
  - `run` (L30-105) e `sellItemStack` (L106-144): passou a inspecionar e vender conteudo interno
    de Shulker Boxes dentro do bau (`fromShulker = true`), chamando `getPrice(item, owner)`
    com o `OfflinePlayer` dono; aplica `AbilityEngine.applySellerSaleAbilities` (multiplicadores,
    venda de pack >=64, bonus shulker, critica, pico de mercado, moeda da sorte e deposito
    direto no Vault quando `deposito_vault` ativo).

[8] SlayerMinionType.kt (252L, diff: +234 linhas)
  - `run` (L42-140): permite operar mesmo sem bau vinculado (quando aplicavel), detecta
    spawners do WildStacker em chunk 3x3 ao redor e aplica `collectNearbyDrops(minion)` (L141-214)
    com agendamento em +2 ticks e +8 ticks para capturar loot apos morte assincrona de stacks.
  - `attackEntityWithSkills` (L215-236): consulta `getSlayerAoeTargets` (`shockwave_cleave`)
    para acertar alvos extras em area e `shouldFrenzyDoubleStrike` (`executioner_frenzy`) para
    disparar segundo golpe imediato.
  - `canAttack` (L237-252): filtra entidades via `EliteMobsProtection.isEliteEntity`, respeita
    regras `damage-animals`, `damage-tamed`, `damage-renamed`, protege `Villager` e `Steerable`,
    ignorando nomes artificiais gerados apenas por regex de stacker.

[9] nms/NMSHandler.kt (diff: +8 linhas)
  - Adiciona metodo abstrato `fun openMinionDialog(player: Player, spec: MinionDialogSpec): Boolean = false`
    para permitir abertura de Dialogs nativos a partir do `:common`.


--------------------------------------------------------------------------------
2.7 MODULO :common - NOVOS SUBSISTEMAS (SKILLS, MENUS, LOGS, DROPS, UTILS)
--------------------------------------------------------------------------------

[1] [NOVO] skills/AbilityEngine.kt (1255L)
  - L36 `object AbilityEngine`: motor central da arvore de habilidades.
  - L39-198: tabelas internas imutaveis de conversao `WOOD_SPECIES`, `SMELT_MAP`, `PLANKS_MAP`,
    `COMPACT_MAP` (count + bloco), `FOSSIL_POOL`, `GEODE_POOL`, `TRASH_FISHER_INPUTS`,
    `TRASH_FISHER_TREASURES`, `VOID_DEFAULT_TRASH`, `RARE_ESSENCE_BY_MOB`.
  - L199-225: data classes de retorno `ActionOutcome`, `SellerOutcome`, `CrafterBonus`, `FisherBonus`.
  - L226-235: `clearCooldowns()`, `pruneExpiredCooldowns()`.
  - L236-450: getters e verificadores matematicos:
    * `getEffectiveRange` (L236), `getEffectiveSpeed` (L265, inclui `ASSEMBLY_SPEED_REDUCE` ate -50%,
      piso 10 ticks), `getEffectiveStorage` (L294), `getEffectiveMultiplier` (L307),
      `getEffectiveLuckBonus` (L331), `getXpMultiplier` (L353), `shouldSaveDurability` (L371, teto 95%),
      `getLumberRareDropMultiplier` (L385), `shouldClearTreeLeaves` (L402),
      `getSlayerAoeTargets` (L415), `shouldFrenzyDoubleStrike` (L428), `isMinerOverdriveTriggered` (L439).
  - L451-517 (`processActionDrops`): processa todos os efeitos `ON_ACTION` (`AUTO_SMELT`,
    `ORE_COMPACT_SMELT`, `LOGS_TO_PLANKS`, `AUTO_COMPACT`, `MULTIPLY_DROPS`, `CANCEL_TOOL_DAMAGE`,
    `DIRECT_TO_CHEST`, `MULTI_KILL_STACK`, `GIVE_MONEY`, `GIVE_XP`, `EXECUTABLE_ITEM`, `GIVE_ITEM`,
    `BONUS_DROP`, `RANDOM_MINERAL_CHEST`, `OBSIDIAN_EXTRA_BREAK`, `BONUS_LOOTING_ROLL`,
    `MOB_HEAD_DROP`, `MOB_RARE_LOOT`, `FISHER_TRASH_TO_TREASURE`, `FISHER_GOLDEN_BONUS`,
    `SEEDS_TO_BONEMEAL`, `GOLDEN_CROP_UPGRADE`, `BONUS_CROP_DROP`, `FOREST_SAP_DROP`,
    `VOID_TRASH`, `CONSOLE_COMMAND`, `PARTICLE`, `SOUND`).
  - L519-550 (`processPeriodicCycle`): processa efeitos `ON_PERIODIC` (`CHEST_AUTO_COMPRESS`,
    `MERGE_GROUND_ITEMS`, `CONTINUOUS_VACUUM`, `MOISTURIZE_FARMLAND`, `BONEMEAL_CROP_AURA`,
    `CROP_EXTRA_GROWTH`, etc.).
  - L551-815: `compactChestForCandidates`, `compactWholeLinkedChest`, `isVoidTrashFiltered`,
    `tryOverflowToOwnerEnderChest`, `collectNearbyXpOrbsIfEnabled`, `processSaplingFertilizer`,
    `shouldSendDropsDirectToChest`, `getExtraStackKillCount`, `applySellerSaleAbilities`,
    `getCrafterAbilityBonus`, `getFisherAbilityBonus`.
  - L1034-1091: `canTriggerAbility` (checa enabled, tier, minLevel, `matchesConditions`,
    probabilidade escalonada por tier e cooldown em ms) e `matchesConditions` (online, mundo,
    material alvo, entidade alvo, encantamento minimo da ferramenta).

[2] [NOVO] skills/SkillEffectFormatter.kt (193L)
  - `formatStatPreviewLines` (L14-114) e `formatAbilityEffectLines` (L115-148): calculam e
    exibem nos tooltips da GUI e do Dialog os valores atuais vs. proximo tier e resumo
    numerico de efeito (`summarizeEffectMagnitude`, L149).

[3] [NOVO] menus/MenuRouter.kt (480L)
  - Cache thread-safe `playerModeCache` (`ConcurrentHashMap<UUID, String>`).
  - Reflection segura para `FloodgateApi` (`isBedrockPlayer`, L43) e `ViaAPI`
    (`supportsDialogProtocol`, L55, checando protocolo >= 771 / 1.21.6).
  - `canUseDialog` (L69), `getEffectiveMenuMode` (L73), `setOrToggleMenuMode` (L88).
  - `openMinionMenu` (L138), `openMainDialogOrFallback` (L147), `openSkillsDialog` (L162),
    `openPickupDialog` (L169) com construtores `buildPickupDialog` (L176), `buildMainDialog`
    (L226) e `buildSkillsDialog` (L358).

[4] [NOVO] menus/MinionActions.kt (138L)
  - Centraliza acoes reutilizadas entre Menu Dialog e GUI Classica: `rotate` (L19),
    `toggleLink` (L28), `upgrade` (L48, com log `UPGRADE`), `collectStorage` (L92),
    `sendUpgradeFail` (L113).

[5] [NOVO] menus/MinionSkillsGui.kt (440L)
  - Quatro `InventoryHolder` dedicados: `SkillsGuiHolder` (54s), `ToolSwapHolder` (27s),
    `PickupGuiHolder` (27s), `AdminMinionsHolder` (54s).
  - `renderBar` (L43), `openSkillsGui` (L53), `openAdminGui` (L143), `openToolSwapGui` (L224),
    `openPickupGui` (L263), `buildStatItem` (L325), `buildAbilityItem` (L364), `makeButton` (L416).

[6] [NOVO] logging/MinionLogger.kt (132L)
  - Buffer circular em memoria (`ConcurrentLinkedDeque`, max 500 linhas) consultavel via
    `getRecentLogs(limit, filter)` (L99) + gravador assincrono em disco por dia
    (`plugins/AxMinions/logs/YYYY-MM-DD.log`) com `BufferedWriter` proprio e metodo
    `shutdown()` (L120). Metodos publicos: `log(minion, category, message)` (L28) e
    `logSystem(...)` (L39).

[7] [NOVO] utils/CustomDropRoller.kt (167L)
  - `roll(minion, effectiveLuck, configPath, maxDrops)` (L30): itera sobre secoes de
    `custom-drops`, aplica filtros `enabled`, `min-level`, `min-luck`, soma chance base +
    `luck-bonus * effectiveLuck`, respeita limite de drops por ciclo (`maxDrops`) e indica
    se deve substituir o drop vanilla (`replaceVanilla`).
  - `createCustomDropItem` (L98): suporta `executable-item` (ExecutableItems), materiais
    Bukkit com `name`, `lore`, `glow`, `enchantments` e executa `commands` do drop com
    placeholders `<player>` e `<amount>`.

[8] [NOVO] utils/ItemUtils.kt (135L)
  - `stripAxMinionsNbt(stack)` (L35): limpa tags `axminions:*` do `PersistentDataContainer` e
    zera `ItemMeta` caso fique efetivamente vazio (`isEffectivelyEmptyMeta`, L64), garantindo
    que itens coletados/minerados empilhem e sejam aceitos por plugins de loja (HeroVender).
  - `buildExecutableItem(id, amount, owner)` (L112): integracao reflexiva segura com
    `ExecutableItemsManager` (`getInstance().getExecutableItem(id).buildItem(amount, owner)`).


--------------------------------------------------------------------------------
2.8 MODULO :common - INTEGRACOES
--------------------------------------------------------------------------------

[1] integrations/Integrations.kt (+40L)
  - NEW L89-97: registra `ItemStackIntegration` quando plugins `ItemStacker` / `ItemStack`
    estiverem ativos e configurados.
  - NEW L108-114: registra `HeroVenderIntegration` quando `HeroVender` estiver presente.
  - Remocao de bloco duplicado do `ItemsAdder` (anteriormente duplicado nas linhas L206-211).

[2] integrations/stacker/WildStackerIntegration.kt (+22L)
  - NEW L16-34 (`calculateStackedExp(entity, baseExp)`): consulta `StackedEntity.getExp(amount, baseExp)`
    com piso garantido de `baseExp * amount`, corrigindo XP subdimensionado ao matar stacks.
  - NEW L37 (`getStackSize(item)`): fallback seguro retornando `item.itemStack.amount.toLong()`.

[3] [NOVO] integrations/prices/HeroVenderIntegration.kt (125L)
  - `register` (L22): localiza reflexivamente a classe principal do HeroVender (`ShopController`)
    e suas versoes de metodo de preco/venda.
  - `getPrice(itemStack)` (L55) e `getPrice(itemStack, player: OfflinePlayer?)` (L59): resolve as
    lojas candidatas para o dono via `resolveCandidateShops` (L87, verificando permissoes de
    shop quando online/fallback para loja padrao) e calcula o maior preco valido para item
    sanitizado sem meta residual do plugin.

[4] [NOVO] integrations/protection/EliteMobsProtection.kt (63L)
  - `isEliteEntity(entity)` (L37): carrega via reflection `com.magmaguy.elitemobs.entitytracker.EntityTracker`
    (`isEliteMob(Entity)`) para impedir que Slayers ataquem ou matem bosses/mobs de evento.

[5] [NOVO] integrations/stacker/ItemStackIntegration.kt (55L)
  - Implementa `StackerIntegration` para o plugin ItemStacker/ItemStack: `getStackSize(entity)`,
    `getStackSize(item)` (via API de quantidade stackada no chao), `setStackSize`, `dropItemAt`.


--------------------------------------------------------------------------------
2.9 MODULO :nms:v26_3
--------------------------------------------------------------------------------

[1] nms/v26_3/DamageHandler.kt (235L -> 131L)
  - Refatoracao completa removendo a criacao da entidade falsa `net.minecraft.world.entity.animal.fox.Fox`
    em NMS (que quebrava/bugava eventos e metadados na 26.3).
  - Novo mecanismo baseado em `ThreadLocal<Minion?>` combinando calculo nativo de atributo de
    ataque + bonus de encantamentos (`Sharpness`, `Smite` para `isUndead`, `Bane of Arthropods`
    para `isArthropod`), bypass de i-frame (invulnerability ticks = 0 antes do dano), limpeza de
    alvo/aggro no mob atacado e evento Bukkit padrao rastreavel pelo `MinionDamageListener`.

[2] nms/v26_3/LootHandler.kt (diff: 67 linhas alteradas)
  - Corrige geracao de loot de pesca na 26.3 implementando pesos vanilla reais de `Luck of the Sea`
    (distribuindo entre `BuiltInLootTables.FISHING_JUNK`, `FISHING_TREASURE` e `FISHING_FISH`)
    envolvidos por bloco try/catch com fallback seguro para `ItemStack(Material.COD)` caso a
    loot table falhe por contexto nulo.

[3] nms/v26_3/NMSHandler.kt (diff: 58 linhas alteradas)
  - Implementa `getExp` e `getDrops` compatíveis com blocos/ferramentas da 26.3.
  - Conecta `openMinionDialog(player, spec)` chamando `DialogMenuHandler.openDialog(player, spec)`.

[4] [NOVO] nms/v26_3/DialogMenuHandler.kt (109L)
  - Usa a Paper 26.3 Registry/Dialog API (`io.papermc.paper.dialog.Dialog`, `DialogBase`,
    `DialogType.multiAction(...)`, `ActionButton`, `DialogAction.customClick(...)`,
    `player.showDialog(...)`).
  - `toComponent` (L35), `openDialog` (L57), `toActionButton` (L90): converte cada
    `DialogButtonSpec` para botao nativo clicavel com callbacks no proprio jogador, icone opcional
    e numero de colunas configuravel.


--------------------------------------------------------------------------------
2.10 ARQUIVOS YAML E TRADUCOES
--------------------------------------------------------------------------------

[1] common/src/main/resources/plugin.yml (+3L)
  - Adiciona `HeroVender` na lista `softdepend`.

[2] common/src/main/resources/config.yml (diff: 156 linhas)
  - `language: pt_BR`, `use-dialog-menu: true`, `config-version: 11`.
  - Traducao completa para Portugues do Brasil e inclusao de hooks `ItemStacker` e
    `HeroVender`, item alimenticio `cooked_beef`, e botoes extras `abilities`, `pickup` e
    `menu_switch` na configuracao de GUI.

[3] common/src/main/resources/messages.yml (diff: 168 linhas)
  - `config-version: 11`. Traduzido para PT-BR.
  - Cores de nivel adicionadas do `11` ao `20`; secao `menu` (troca Dialog/GUI e avisos
    Bedrock/versao antiga); secao `skills` (upgrade, pontos, nivel minimo, respec); secao
    `pickup` (`with-tool` / `without-tool`); `errors.give-max-level`; `tools.stored-lore` e
    mapa completo `tool-names` em Portugues.

[4] [NOVOS] common/src/main/resources/lang/*.yml (11 arquivos, 156 linhas cada, config-version: 11)
  - `ar_SA.yml`, `bn_BD.yml`, `de_DE.yml`, `en_US.yml`, `es_ES.yml`, `fr_FR.yml`, `hi_IN.yml`,
    `id_ID.yml`, `pt_BR.yml`, `ru_RU.yml`, `zh_CN.yml`. Todos sincronizados com as chaves
    mais novas da versao `11`.

[5] common/src/main/resources/minions/*.yml (8 arquivos, todos em config-version: 2)
  - `collector.yml` (HEAD 134L -> NEW 424L): Lv 1..20, `upgrades` Lv 1 (`range 1.5, speed 200, storage 200`)
    ate Lv 20 (`range 2.0, speed 125, storage 12000`), 4 stats (`speed`, `range`, `durability_save`, `storage`)
    e 8 habilidades (`L292-423`).
  - `crafter.yml` (HEAD 123L -> NEW 376L): Lv 1..20 (`speed 200->105`, `storage 200->12000`), 2 stats
    (`speed`, `storage`) e 8 habilidades (`L247-375`).
  - `farmer.yml` (HEAD 146L -> NEW 423L): Lv 1..20 (`range 1.0->1.5`, `speed 240->140`, `storage 200->12000`),
    4 stats (`speed`, `range`, `durability_save`, `storage`) e 8 habilidades (`L294-422`).
  - `fisher.yml` (HEAD 133L -> NEW 1055L): Lv 1..20 (`range 2`, `speed 220->145`, `storage 200->12000`),
    4 stats (`speed`, `range`, `luck`, `durability_save`), 8 habilidades (`L287-416`),
    `custom-drops-max-per-catch: 1` (L417) e `custom-drops` com 62 entradas (`L433-1054`).
  - `lumber.yml` (HEAD 138L -> NEW 481L): Lv 1..20 (`range 1.5->2.0`, `speed 240->150`, `storage 200->12000`),
    4 stats (`speed`, `range`, `durability_save`, `storage`), 8 habilidades (`L292-422`),
    `custom-drops-max-per-catch: 1` (L423) e `custom-drops` com 8 entradas (`L425-480`).
  - `miner.yml` (HEAD 161L -> NEW 464L): `whitelist` expandida de ~6 para 35 blocos (`L19-55`,
    cobrindo minérios de Deepslate, Nether, Ancient Debris, Basalt, Blackstone, End Stone,
    Amethyst, Glowstone, Crying Obsidian); Lv 1..20 (`range 1`, `speed 220->140`, `storage 200->12000`),
    4 stats (`speed`, `range`, `durability_save`, `storage`) e 8 habilidades (`L334-463`).
  - `seller.yml` (HEAD 148L -> NEW 416L): Lv 1..20 (`multiplier 1.0->1.33`, `speed 600->300`,
    `storage 100k->500M`), 3 stats (`speed`, `multiplier`, `storage`) e 8 habilidades (`L284-415`).
  - `slayer.yml` (HEAD 144L -> NEW 429L): Lv 1..20 (`range 1.5->2.0`, `speed 220->145`,
    `storage 800->60000`), 4 stats (`speed`, `range`, `durability_save`, `storage`) e 8
    habilidades (`L297-428`).


================================================================================
COMO SUBIR ESTE REPOSITORIO COMO FORK NO GITHUB (OPCIONAL QUANDO QUISER)
================================================================================
  1. Verifique o estado limpo com `git status`.
  2. Crie um repositorio no seu GitHub (ex: `Gstormtravel/AxMinions`).
  3. Troque/adicione o remote da sua fork e crie o commit da versao 26.3:
       git remote rename origin upstream
       git remote add origin https://github.com/Gstormtravel/AxMinions.git
       git checkout -b mc-26.3-fork
       git add .
       git commit -m "Fork AxMinions 1.0.50-GS-DEFINITIVA (MC 26.3 + Skills + Dialog + HeroVender)"
       git push -u origin mc-26.3-fork

================================================================================
FIM DO DOCUMENTO - AXMINIONS 1.0.50-GS-DEFINITIVA (26.3)
================================================================================
