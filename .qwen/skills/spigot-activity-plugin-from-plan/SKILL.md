---
name: spigot-activity-plugin-from-plan
description: 从中文开发方案快速落地 Spigot 1.12.2/Java 8 活动插件，并用 Maven 和本地测试服验证。
source: auto-skill
extracted_at: '2026-06-10T00:09:07.295Z'
---

# 从方案编写 Spigot 活动插件

## 适用场景

当项目只有一份中文开发方案（例如活动玩法、命令、GUI、配置、数据结构），需要快速生成一个可构建的 Spigot/Paper 1.12.2 Java 8 插件。

## 可复用流程

1. **先读完整方案，不要只看开头**
   - 方案可能前面写核心玩法，后面才写配置、数据存储、权限和开发优先级。
   - 先提取：插件名、MC/Spigot 版本、Java 版本、命令、权限、配置文件、数据格式、优先级。

2. **确认插件类型，避免套错模板**
   - 如果用户环境里有 Slimefun 技能，不代表当前方案一定是 Slimefun 附属。
   - 本次方案实际是独立端午节活动插件，虽然开局加载了“粘液科技附属”技能，但实现时应按普通 Spigot 插件处理，不添加 Slimefun 依赖。

3. **按最小可运行结构生成 Maven 项目**

   ```text
   pom.xml
   src/main/java/<package>/
     <PluginMain>.java
     command/
     data/
     gui/
     listener/
     race/ 或 feature-specific package
   src/main/resources/
     plugin.yml
     config.yml
     messages.yml
     shop.yml
   ```

4. **Spigot 1.12.2 注意点**
   - Maven 依赖使用 `org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT`，scope 为 `provided`。
   - 编译目标设置 Java 8。
   - 不使用新版 Bukkit API，例如避免 `Material.isAir()` 这类新版本方法。
   - 物品材质名使用 1.12.2 名称，例如 `SKULL_ITEM`、`FIREWORK`、`CROPS`、`BEETROOT_BLOCK`。

5. **数据存储优先实现 YAML + 内存缓存**
   - `playerdata/<UUID>.yml` 存储积分、签到日期等需要跨重启保存的玩家状态。
   - 如果活动材料要求是“实体物品”，不要再把材料数量存进 `PlayerData`；用背包里的自定义 `ItemStack` 作为事实来源。
   - 玩家进服加载、退服保存。
   - 定时异步保存全部缓存数据。
   - 插件关闭时同步保存全部数据。

6. **Spigot 1.12.2 实体活动材料实现建议**
   - 1.12.2 没有 `PersistentDataContainer`，自定义材料可用 `ItemMeta` 的 `displayName` + 隐藏/灰色 lore marker 识别，例如 `ChatColor.DARK_GRAY + "PluginName:" + materialKey`。
   - 封装一个专门工具类（如 `MaterialItems`）统一处理：`create(type, amount)`、`give(player, type, amount)`、`count(player, type)`、`has(player, cost)`、`take(player, cost)`。
   - `give` 用 `player.getInventory().addItem(...)`，把 `remaining` 掉落到玩家位置，避免背包满时丢失材料。
   - `count/has/take` 只匹配带 marker 的自定义材料，不要仅按原版 `Material` 匹配，避免误统计或误消耗普通物品。
   - `take` 修改背包内容后调用 `player.updateInventory()`，提升旧版本客户端同步稳定性。
   - 管理员给予命令要校验数量 `>= 1`，避免生成 0 或负数 `ItemStack`。
   - 从虚拟材料迁移到实体材料时，替换所有入口：活动掉落/钓鱼奖励、管理员 `/give`、`/materials` 查询、制作消耗；然后全局搜索旧方法名（如 `addMaterial/getMaterial/hasMaterials/takeMaterials/materials.`）确认无残留。
   - 如果材料不再持久化，清理 `PlayerData` 和 `PlayerDataManager` 中的材料字段读写，并同步用户文档说明“材料在背包中，不保存在玩家数据文件”。

7. **命令实现建议**
   - 一个主命令类同时实现 `CommandExecutor` 和 `TabCompleter`。
   - 管理命令先检查 `duanwu.admin`。
   - 玩家命令先 `requirePlayer`，避免控制台触发玩家逻辑。
   - 对数字、玩家、枚举材料做显式校验，失败返回友好提示。
   - 玩家高频入口尽量做到"无参数打开 GUI、有参数快速执行"：例如 `/duanwu make` 打开制作菜单，`/duanwu make normal|luxury` 保留快速制作，避免玩家必须记忆所有子参数。
   - **管理命令的 Tab 补全不要只补一级子命令**：`addpoint`、`setpoint`、`open`、`give` 等需要指定玩家的命令，二级参数应补全在线玩家名。写法：
     ```java
     if (args.length == 2 && ("addpoint".equalsIgnoreCase(args[0]) || "setpoint".equalsIgnoreCase(args[0]))) {
         List<String> players = new ArrayList<>();
         for (Player player : plugin.getServer().getOnlinePlayers()) {
             players.add(player.getName());
         }
         return filter(players, args[1]);
     }
     ```
     注意：`onTabComplete` 中不要用 `sender.hasPermission()` 过滤，因为控制台执行时也需要补全。权限检查交给 `onCommand` 执行时处理即可。

8. **玩家视角 GUI 补全建议**
   - 如果用户反馈“玩家视角不够完整”，优先检查核心闭环是否只有命令没有界面：材料查询、制作、商店、签到、竞速是否能让普通玩家理解下一步。
   - 制作类玩法适合新增常驻监听器 GUI（如 `MakeMenu implements Listener`）：`open(player)` 创建 Inventory；`InventoryClickEvent` 检查标题、取消点击、按 slot 判断制作类型。
   - GUI lore 应显示：材料需求、玩家当前拥有数量、积分收益、是否足够；材料不足时仅提示，不扣除。
   - 把实际扣材料/加积分逻辑封装成可复用方法（如 `craft(player, type)`），命令快速制作和 GUI 点击共用，避免两套逻辑漂移。
   - 新 GUI 要在主插件 `onEnable` 中实例化并 `registerEvents`，并提供 getter 让命令类调用。
   - 打开商店/制作菜单这类玩家动作最好发送反馈消息（如 `shop-open`、`make-menu-open`），提升操作确认感。

9. **从“可用版”升级到“精品版”的第一优先级**
   - 当用户反馈“太简陋、要精品版/顶尖插件”时，不要只继续堆功能；先补一段明确的精品版升级方案，列出玩家体验、活动目标、玩法深度、兼容增强、文档配置升级，让后续开发有路线。
   - 第一阶段优先做“玩家不用问就知道怎么玩”的体验层：`/duanwu` 默认打开活动中心主菜单，而不是只显示文字 help。
   - 活动中心主菜单建议包含：我的状态、制作粽子、活动商店、每日签到、龙舟竞速、幸运摸鱼说明、新手指南；每个入口都能点击进入下一步或执行命令。
   - 增加 `/duanwu guide` 给小白玩家展示完整闭环：收集材料 → 查看材料 → 制作粽子 → 获得积分 → 兑换奖励，并强调普通原版物品不能当活动材料。
   - 增加 `/duanwu status` 显示玩家当前积分、背包材料、签到状态、竞速状态和“推荐下一步”，比单独 `/points`、`/materials` 更有引导性。
   - 增加 `/duanwu fish` 或活动说明页，用玩家语言解释概率玩法，同时让概率和礼包命令保持配置化。
   - 首次/每次进服引导可用 `PlayerJoinEvent` + `runTaskLater` 延迟发送，配置项如 `guide.join-message`、`guide.join-delay-seconds`、`guide.join-lines`，避免玩家登录瞬间刷屏丢失信息。
   - 玩家文档要按“小白”写：先给一句话流程和第一步命令，再给示例、注意事项、常见坑；把管理员配置章节放后面，避免玩家被配置细节劝退。
   - 菜单点击可以调用命令类中的公共说明方法（如 `sendGuide/sendStatus/sendFishGuide`）以复用文字逻辑，但要注意避免循环依赖：主插件保存 command 实例并提供 getter。

10. **GUI 商店实现建议**
   - 从 `shop.yml` 读取物品配置：name、material、points、lore、commands。
   - 打开 Bukkit Inventory。
   - `InventoryClickEvent` 里检查标题、取消点击、扣积分、执行控制台命令。
   - 命令中用 `{player}` 占位替换玩家名。

11. **玩法监听实现建议**
   - `BlockBreakEvent`：矿物给糯米、树叶给粽叶、作物给红枣。
   - `EntityDeathEvent`：怪物给糯米，动物给鲜肉。
   - `PlayerFishEvent`：按概率给材料或礼包命令奖励。
   - 所有概率从 `config.yml` 读取，默认关闭或低概率，避免硬编码不可调。
   - 钓鱼这类概率表不要写死 `random.nextInt(100)` 的区间；读取 `fish-rewards.*` 后按总权重抽取，允许管理员调整权重总和。
   - 礼包类钓鱼奖励也应从配置读取命令列表（如 `fish-commands.lucky`、`fish-commands.legend`），不要硬编码 `give chest`/`give diamond`。

12. **赛道/排行榜这类 V1.1+ 功能可先做基础可用版**
   - 用 `race.yml` 保存起点、终点、排行榜记录。
   - 设置命令最好明确成 `race create start|end`，避免同一个命令无法判断下一次是起点还是终点。
   - 玩家 `join` 后记录开始时间，在 `PlayerMoveEvent` 中靠近终点自动完成并更新最佳成绩。

13. **构建验证**
    - 完成后运行：

      ```bash
      mvn clean package
      ```

    - 成功产物通常在：

      ```text
      target/<PluginName>-<version>.jar
      ```

14. **本地 Paper/Spigot 测试服验证**
    - 先检查测试服目录：服务端 jar、`plugins/`、`eula.txt`、启动脚本、`logs/latest.log`。
    - 把构建产物复制到测试服 `plugins/` 后再启动服务端。
    - 在 Qwen Code 的 Windows 环境中，工具 shell 可能是 Bash：
      - `Copy-Item` 直接运行会失败，因为那是 PowerShell 命令。
      - `cmd /c copy` 可能在某些环境下进入交互式 `cmd` 而不返回。
      - 更稳的方式是显式调用：

        ```bash
        powershell.exe -NoProfile -Command "Copy-Item -LiteralPath 'D:\path\target\Plugin.jar' -Destination 'E:\server\plugins\Plugin.jar' -Force"
        ```

    - 如果工具限制不允许把 `directory` 设置到测试服路径，可从项目目录运行并在命令里切目录：

      ```bash
      powershell.exe -NoProfile -Command "Set-Location -LiteralPath 'E:\server'; java -jar 'paper.jar' nogui"
      ```

    - 启动后读 `logs/latest.log`，确认出现：
      - `[PluginName] Loading ...`
      - `[PluginName] Enabling ...`
      - 插件自己的启用日志
    - 检查 `plugins/<PluginName>/` 是否生成默认配置文件，作为资源释放验证。
    - 日志里的 `ERROR` 要归因，不要只看关键词；例如同服 Slimefun/CS-CoreLib 自动更新失败可能与当前插件无关。
    - 测试结束后停止后台服务端进程，避免 Java 长时间占用测试服目录和端口。

## 代码质量审查清单（从"能用"到"顶尖"）

当用户问"距离顶尖插件差多少"或要求代码审查时，按以下维度系统评估：

### 1. 代码健壮性（最容易出问题的点）

| 检查项 | 说明 |
| --- | --- |
| **GUI 标题字符串比较** | 用 `TITLE.equals(event.getInventory().getTitle())` 判断点击事件？玩家改语言包或重名 GUI 会串。应改用 `event.getView().getTitle()` 或 Inventory 引用比较。 |
| **事件监听性能** | `PlayerMoveEvent` 每 tick 触发，里面是否有 `distanceSquared` 等计算？100 人同时在线时是否扛得住？考虑加 `from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()` 提前过滤。 |
| **IO 是否阻塞主线程** | `onDisable` 和 `PlayerQuitEvent` 中是否同步写文件？应异步保存，`onDisable` 可加超时保护。 |
| **物品识别方式** | 1.12.2 没有 PDC，用 Lore marker 识别自定义物品时，`contains()` 匹配是否可靠？如果 lore 顺序变化会漏判。 |
| **BOSS/实体识别** | 是否用 `getCustomName()` 字符串匹配？玩家用命名牌可冒充。应用 `Metadata` 或 `Scoreboard Tag`。 |
| **排行榜 key** | 用 `player.getName()` 做 key？玩家改名后记录丢失。应用 UUID。 |
| **数字解析** | `parseInt` 返回 `null` 还是抛异常？风格是否一致。 |
| **配置读取** | 直接 `getInt()` 不校验？配置写错类型会抛异常。启动时应校验配置合法性。 |

### 2. 架构设计

| 检查项 | 说明 |
| --- | --- |
| **软依赖接入** | `plugin.yml` 声明了 `softdepend`，但代码里是否实际检测并接入？用 `getServer().getPluginManager().getPlugin("PlaceholderAPI")` 判断。 |
| **API 暴露** | 其他插件能否通过 `Bukkit.getPluginManager().getPlugin()` 调用本插件功能？应有 `getAPI()` 接口类。 |
| **错误处理** | 每个 IO 操作、命令执行是否有 try-catch 和日志？还是裸奔。 |
| **异步架构** | 数据读写是否全异步？还是只有定时保存是异步。 |
| **配置热重载安全** | 重载前是否校验配置合法性？失败是否回滚？还是直接覆盖所有对象。 |

### 3. 功能完整性

| 检查项 | 说明 |
| --- | --- |
| **PlaceholderAPI** | 是否注册了 `%duanwu_*%` 全套变量？ |
| **Vault 经济** | 商店奖励是用命令 `eco give` 依赖具体经济插件，还是原生 Vault API？ |
| **PlayerPoints** | 是否原生支持？ |
| **签到算法** | 是连续签到还是 `getDayOfYear() % N + 1` 这种每月重置的算法？ |
| **BOSS 防作弊** | 是否有反绕过机制？ |

### 4. 审查输出格式

按以下结构输出，让用户一目了然：

```
功能完整度: ████████░░  80%
代码健壮性: █████░░░░░  50%
性能表现:   ██████░░░░  60%
可维护性:   █████░░░░░  50%
配置灵活度: ████████░░  80%
兼容性:     ████░░░░░░  40%
```

然后按优先级列出修复建议：
- 🔴 紧急修复 — 影响运行稳定性和数据安全
- 🟡 重要改进 — 影响体验和可维护性
- 🟢 锦上添花 — 体验增强

### 5. 修复实施优先级（从实际修复中总结）

当用户要求"修复"时，按以下顺序推进：

1. **🔴 紧急修复（影响运行稳定性和数据安全）**
   - GUI 标题碰撞 → 用 `InventoryHolder` 替代字符串匹配
   - `PlayerMoveEvent` 性能 → 加 `blockX/blockZ` 变化过滤
   - BOSS/实体识别 → 用 `Metadata`（`FixedMetadataValue`）替代 `CustomName`
   - 排行榜 key → 用 UUID 替代玩家名

2. **🟡 重要改进（影响体验和可维护性）**
   - 签到算法 → 用累计签到天数取模，不用日期取模
   - 软依赖接入 → PlaceholderAPI 变量注册、Vault 经济原生支持
   - IO 异步化 → `PlayerQuitEvent` 的 `unload` 改为异步
   - 异常保护 → 事件监听器加 `try-catch` 顶层保护

3. **🟢 锦上添花（体验增强）**
   - 配置启动校验
   - API 暴露给其他插件
   - 多语言支持

### 6. 软依赖接入方案（编译期不依赖外部 jar）

当 `plugin.yml` 声明了 `softdepend` 但项目 pom.xml 没有添加对应依赖时，用反射方式接入：

**Vault 经济：**

```java
// 运行时反射获取 Economy 服务，编译期不依赖 Vault jar
Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
Object provider = plugin.getServer().getServicesManager().getRegistration(economyClass);
Method getProvider = provider.getClass().getMethod("getProvider");
Object economy = getProvider.invoke(provider);
Method balanceMethod = economyClass.getMethod("getBalance", OfflinePlayer.class);
Method withdrawMethod = economyClass.getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
// 调用时反射 invoke
double balance = (double) balanceMethod.invoke(economy, player);
```

**PlaceholderAPI：**

```java
// 运行时检测 PlaceholderAPI 是否存在
boolean hasPAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
```

PlaceholderAPI 的 `PlaceholderExpansion` 是抽象类，编译期不依赖时有两种方案：
- **方案 A（推荐）**：在 pom.xml 中添加 PlaceholderAPI 为 `provided` 依赖，直接继承 `PlaceholderExpansion`
- **方案 B（无依赖）**：使用 `javax.tools.JavaCompiler` 在运行时动态编译子类（需要 JDK，不推荐生产环境）

实际项目中，如果用户没有在 pom.xml 中添加这些依赖，优先用**反射方案**接入 Vault，PlaceholderAPI 则用运行时检测 + 动态编译方案。

### 7. 修复后的质量评分更新

修复后按以下维度重新评估，让用户看到进步：

```
代码健壮性: 50% → 75%
性能表现:   60% → 80%
兼容性:     40% → 70%
```

## 经验教训

- 不要因为外部上下文存在某个技能就强行按该技能实现；必须以用户项目内方案为准。
- Windows 环境下工具 shell 可能实际是 Bash，PowerShell 的 `New-Item`/`Copy-Item` 会失败；需要显式调用 `powershell.exe`，或使用跨 shell 命令。
- 测试服路径可能不在注册 workspace 内；不要直接把工具工作目录设到该路径，改用命令内部 `Set-Location`。
- 判断服务端测试结果时，以插件加载/启用日志、配置目录生成、命令注册表现为准；其他插件的报错要单独归因。
- 对"根据方案编写插件"这类宽泛请求，先实现方案中的 V1.0 核心闭环，再把 V1.1+ 做成基础可用版本，避免过度扩展。
- **修复代码质量问题时，先做全面代码审查再动手**，不要边看边改。一次性列出所有问题并按优先级排序，让用户确认后再实施，避免改到一半发现还有更严重的问题。
- **软依赖接入不要硬编码编译期依赖**。如果 pom.xml 没有添加 Vault/PlaceholderAPI 的依赖，用反射方式接入，确保插件在没有这些依赖的服务器上也能正常加载。
- **`InventoryHolder` 是 GUI 标识的最佳实践**。用 `Bukkit.createInventory(new MenuHolder("id"), size, title)` 替代字符串标题匹配，避免语言包或重名 GUI 导致事件串扰。
- **`PlayerMoveEvent` 一定要加位置变化过滤**。`from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()` 可以过滤掉 99% 的旋转/小幅移动事件。
- **排行榜/记录类数据用 UUID 做 key**，同时存玩家名用于显示。兼容玩家改名场景，旧版玩家名 key 的数据在加载时忽略。
- **签到天数用累计次数取模**（`signDays % N + 1`），不要用日期取模（`getDayOfYear() % N + 1`），后者每月/每年重置。
- **事件监听器加 try-catch 顶层保护**，防止单个事件异常导致整个监听器崩溃，影响其他玩家。
- **删除功能模块时要全面清理引用**：删除一个功能（如龙舟竞速）需要检查：功能类本身、主类引用、命令注册和 tab 补全、事件监听器、GUI 按钮和点击处理、plugin.yml 权限、config.yml/messages.yml 配置。漏掉任何一处都会导致编译错误或运行时异常。
- **`messages.yml` 中残留的旧消息 key 不影响运行**，只是不再被引用。可以保留作为兼容，也可以清理。如果清理，确保代码中没有任何地方再引用这些 key。
