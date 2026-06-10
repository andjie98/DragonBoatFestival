# DragonBoatFestival 用户 Wiki

DragonBoatFestival 是一个适用于 Spigot/Paper 1.12.2 的端午节活动插件。玩家通过日常行为收集粽子材料、制作粽子获得节日积分，使用积分在活动商店兑换奖励。支持 BOSS 活动、全服目标、PlaceholderAPI 占位符和 Vault 经济。

当前版本：`1.0.0`

---

## 1. 安装方式

1. 将插件文件放入服务器插件目录：

   ```text
   plugins/DragonBoatFestival-1.0.0.jar
   ```

2. 启动服务器。
3. 插件会自动生成配置目录：

   ```text
   plugins/DragonBoatFestival/
   ├── config.yml
   ├── messages.yml
   ├── shop.yml
   ├── goals.yml        # 全服目标进度（自动生成）
   └── playerdata/
       └── UUID.yml     # 玩家数据
   ```

4. 如果控制台出现以下日志，表示插件已正常启用：

   ```text
   [DragonBoatFestival] DragonBoatFestival 已启用
   ```

---

## 2. 玩家玩法

玩家只需要记住一句话：

```text
做日常拿材料 → 打开活动中心 → 做粽子拿积分 → 用积分换奖励
```

### 2.1 打开活动中心

最推荐玩家使用这个命令：

```text
/duanwu
```

它会打开"端午活动中心"GUI，里面可以直接进入：

- 我的活动状态
- 制作粽子
- 活动商店
- 每日签到
- 幸运摸鱼说明
- 新手指南

如果玩家不知道下一步做什么，可以输入：

```text
/duanwu status
```

插件会告诉玩家当前积分、材料、签到状态，并给出下一步推荐。

---

### 2.2 新手指南

```text
/duanwu guide
```

适合第一次参加活动的玩家。它会按步骤告诉玩家：

1. 去哪里拿材料。
2. 怎么查看材料。
3. 怎么制作粽子。
4. 怎么获得积分。
5. 怎么兑换奖励。

玩家刚进服时，如果活动开启且 `guide.join-message` 为 `true`，插件也会自动发送一段小白引导。

---

### 2.3 查看活动帮助

```text
/duanwu help
```

也可以使用别名：

```text
/dw help
```

---

### 2.4 收集粽子材料

玩家进行日常行为时，有概率获得活动材料。

| 材料 | 获取方式 | 默认概率 |
| --- | --- | --- |
| 糯米 | 挖矿、击杀怪物、钓鱼 | 挖矿/打怪 5%，钓鱼概率见下文 |
| 粽叶 | 破坏树叶、钓鱼 | 破坏树叶 5%，钓鱼概率见下文 |
| 红枣 | 采集作物、钓鱼 | 采集作物 3%，钓鱼概率见下文 |
| 鲜肉 | 击杀动物、钓鱼 | 击杀动物 3%，钓鱼概率见下文 |

钓鱼奖励概率：

| 奖励 | 概率 |
| --- | --- |
| 糯米 | 40% |
| 粽叶 | 30% |
| 红枣 | 15% |
| 鲜肉 | 10% |
| 幸运礼包 | 4% |
| 传说礼包 | 1% |

钓鱼概率由 `config.yml` 的 `fish-rewards` 配置控制；幸运礼包和传说礼包执行的奖励命令由 `fish-commands.lucky`、`fish-commands.legend` 配置控制。

玩家获得材料时，会收到提示消息。材料会作为带有名称和说明的实体物品进入玩家背包；如果背包已满，多出的材料会掉落在玩家当前位置附近。

---

### 2.5 查看材料

```text
/duanwu materials
```

显示当前背包内拥有的端午材料实体物品数量：

- 糯米
- 粽叶
- 红枣
- 鲜肉

只有插件发放的端午材料物品会被统计，普通原版物品不会被当作材料。

---

### 2.6 制作粽子

打开制作 GUI：

```text
/duanwu make
```

菜单会显示普通粽子、豪华粽子的材料需求、当前拥有数量和可获得积分，点击对应物品即可制作。

也可以使用命令快速制作：

#### 普通粽子

```text
/duanwu make normal
```

默认消耗：

| 材料 | 数量 |
| --- | --- |
| 糯米 | 5 |
| 粽叶 | 2 |

默认获得：

```text
1 节日积分 + 普通粽子物品（可右键食用）
```

---

#### 豪华粽子

```text
/duanwu make luxury
```

默认消耗：

| 材料 | 数量 |
| --- | --- |
| 糯米 | 10 |
| 粽叶 | 5 |
| 红枣 | 3 |
| 鲜肉 | 3 |

默认获得：

```text
5 节日积分 + 豪华粽子物品（可右键食用）
```

制作时会从玩家背包中扣除对应端午材料实体物品。普通原版物品不会被消耗。

制作获得的粽子物品可以右键食用恢复饥饿值，也可以与其他玩家交易。

---

### 2.7 查看节日积分

```text
/duanwu points
```

节日积分可用于活动商店兑换奖励。

---

### 2.8 打开活动商店

```text
/duanwu shop
```

商店会打开 GUI 菜单。点击商品后，如果积分足够，会扣除积分并执行对应奖励命令。

商店商品完全通过 `shop.yml` 自定义，服主可以任意增删改商品。默认商品：

| 商品 | 需要积分 | 默认奖励命令 |
| --- | ---: | --- |
| 金币礼包 | 10 | `eco give {player} 1000` |
| 幸运钥匙 | 20 | `give {player} tripwire_hook 1` |
| 节日烟花 | 15 | `give {player} fireworks 3` |
| 端午纪念头颅 | 50 | `give {player} skull_item 1` |
| 限定称号 | 100 | `title {player} title {"text":"端午勇士","color":"green"}` |

`{player}` 会自动替换为玩家名。

---

### 2.9 每日签到

```text
/duanwu sign
```

每天只能签到一次。签到奖励采用 5 天循环制，由 `config.yml` 中的 `sign-rewards` 配置决定。

默认签到奖励包含：

- 金币命令奖励
- 节日积分
- 幸运钥匙
- 经验
- 标题奖励

---

### 2.10 BOSS 活动

管理员可召唤端午 BOSS：

```text
/duanwu boss spawn
```

BOSS 会在玩家当前位置生成。BOSS 类型、血量、名称、奖励积分、击杀命令均可由 `config.yml` 配置。

击杀 BOSS 的玩家获得积分奖励，全服广播公告。

---

### 2.11 全服目标

插件自带全服目标系统：

| 目标 | 默认目标值 | 说明 |
| --- | ---: | --- |
| 全服制作粽子 | 500 | 所有玩家制作粽子次数合计 |
| 全服每日签到 | 100 | 所有玩家签到次数合计 |
| 击败端午 BOSS | 1 | 击杀 BOSS 次数 |

目标完成后全服广播并执行奖励命令。目标进度保存在 `goals.yml` 中，支持延迟批量写入。

---

## 3. 管理员命令

管理员命令需要权限：

```text
duanwu.admin
```

### 3.1 重载配置

```text
/duanwu reload
```

重载：

- `config.yml`
- `messages.yml`
- `shop.yml`
- 全服目标
- 商店菜单

---

### 3.2 为玩家打开商店

```text
/duanwu open 玩家名
```

示例：

```text
/duanwu open Steve
```

---

### 3.3 给予玩家材料

```text
/duanwu give 玩家名 材料 数量
```

材料名：

| 参数 | 材料 |
| --- | --- |
| `rice` | 糯米 |
| `leaf` | 粽叶 |
| `jujube` | 红枣 |
| `meat` | 鲜肉 |

该命令会把对应端午材料实体物品放入玩家背包；背包已满时，多出的物品会掉落在玩家当前位置附近。被给予的玩家也会收到通知。

示例：

```text
/duanwu give Steve rice 10
/duanwu give Steve leaf 5
/duanwu give Steve jujube 3
/duanwu give Steve meat 3
```

---

### 3.4 增加玩家积分

```text
/duanwu addpoint 玩家名 数量
```

示例：

```text
/duanwu addpoint Steve 50
```

---

### 3.5 设置玩家积分

```text
/duanwu setpoint 玩家名 数量
```

示例：

```text
/duanwu setpoint Steve 100
```

积分不会低于 0。

---

### 3.6 召唤 BOSS

```text
/duanwu boss spawn
```

在玩家当前位置生成 BOSS。

---

## 4. 权限节点

| 权限 | 默认 | 说明 |
| --- | --- | --- |
| `duanwu.admin` | OP | 管理员命令 |
| `duanwu.sign` | 所有玩家 | 每日签到 |
| `duanwu.shop` | 所有玩家 | 打开活动商店 |
| `duanwu.make` | 所有玩家 | 制作粽子 |
| `duanwu.guide` | 所有玩家 | 查看活动指南和状态 |
| `duanwu.goals` | 所有玩家 | 查看全服任务进度 |

---

## 5. 配置文件说明

### 5.1 `config.yml`

#### 活动开关

```yaml
festival:
  enabled: true
```

设置为 `false` 后，掉落逻辑和大部分活动命令会停止。

---

#### 自动保存间隔

```yaml
auto-save-seconds: 300
```

单位：秒。插件会定时异步保存在线玩家数据，实际最小值为 60 秒。

---

#### 粽子积分

```yaml
points:
  normal-zongzi: 1
  luxury-zongzi: 5
```

控制制作粽子后获得的节日积分。

---

#### 材料掉落概率

```yaml
drop:
  rice: 5    # 糯米 %
  leaf: 5    # 粽叶 %
  jujube: 3  # 红枣 %
  meat: 3    # 鲜肉 %
```

数值表示百分比概率。

---

#### 制作消耗

```yaml
make:
  normal:
    rice: 5
    leaf: 2
  luxury:
    rice: 10
    leaf: 5
    jujube: 3
    meat: 3
```

控制普通粽子和豪华粽子的材料消耗。

---

#### 签到奖励（5 天循环）

```yaml
sign-rewards:
  day1:
    message: '&a签到成功，获得金币奖励。'
    commands:
      - 'eco give {player} 100'
  day2:
    points: 5
    message: '&a签到成功，获得 5 节日积分。'
```

支持字段：

| 字段 | 说明 |
| --- | --- |
| `message` | 签到成功提示 |
| `points` | 增加节日积分 |
| `exp` | 增加经验 |
| `commands` | 控制台执行命令列表（`{player}` 替换为玩家名） |

---

#### 钓鱼奖励

```yaml
fish-rewards:
  rice: 40    # 糯米概率权重
  leaf: 30    # 粽叶概率权重
  jujube: 15  # 红枣概率权重
  meat: 10    # 鲜肉概率权重
  lucky: 4    # 幸运礼包概率权重
  legend: 1   # 传说礼包概率权重
```

奖励类型使用权重随机算法，各数值相加为总权重。

---

#### 钓鱼命令

```yaml
fish-commands:
  lucky:
    - 'give {player} chest 1'
  legend:
    - 'give {player} diamond 3'
```

钓到幸运/传说礼包时，由控制台执行的命令列表。

---

#### BOSS 配置

```yaml
boss:
  enabled: true
  type: ZOMBIE        # 实体类型
  name: '&2端午粽王'   # 显示名称
  health: 200          # 血量
  reward-points: 20    # 击杀奖励积分
  commands:
    - 'give {player} emerald 3'
```

---

#### 全服目标

```yaml
server-goals:
  make-zongzi:
    enabled: true
    name: '全服制作粽子'
    target: 500
    complete-message: '&a全服任务完成：&e{name}&a！'
    commands:
      - 'broadcast 端午全服制作任务完成！'
  sign:
    enabled: true
    name: '全服每日签到'
    target: 100
    ...
  boss-kill:
    enabled: true
    name: '击败端午 BOSS'
    target: 1
    ...
```

每个目标支持：开关、名称、目标值、完成提示、完成命令。

---

### 5.2 `messages.yml`

用于修改插件提示文本。颜色代码使用 `&`。

```yaml
prefix: '&a[端午节]&r '
no-permission: '&c你没有权限执行该命令。'
```

---

### 5.3 `shop.yml`

用于配置活动商店商品。**完全自定义**，服主可任意增删改。

```yaml
items:
  coin:
    name: '&e金币礼包'
    material: GOLD_INGOT
    points: 10
    lore:
      - '&7兑换金币礼包'
      - '&7需要积分：&e10'
    commands:
      - 'eco give {player} 1000'
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `name` | 商品显示名（支持 `&` 颜色码） |
| `material` | GUI 中显示的物品材质（Bukkit Material 名） |
| `points` | 兑换需要的积分 |
| `lore` | 商品介绍，每行一条 |
| `commands` | 兑换成功后由控制台执行的命令（`{player}` 替换为玩家名） |

修改后执行：

```text
/duanwu reload
```

---

## 6. 数据存储

玩家数据保存在：

```text
plugins/DragonBoatFestival/playerdata/玩家UUID.yml
```

保存内容包括：

- 玩家名
- 节日积分
- 制作统计（普通/豪华粽子数、钓鱼次数、商店兑换次数）
- 签到天数
- 最后签到日期

端午材料以背包物品形式保存（通过 lore 标记识别），**不在 YAML 文件中存储**。

全服目标进度保存在：

```text
plugins/DragonBoatFestival/goals.yml
```

---

## 7. PlaceholderAPI 占位符

如果服务器安装了 PlaceholderAPI，插件会自动注册以下变量：

| 占位符 | 说明 | 排行榜适用 |
| --- | --- | --- |
| `%duanwu_points%` | 节日积分 | ✅ |
| `%duanwu_normal_made%` | 普通粽子制作数 | ✅ |
| `%duanwu_luxury_made%` | 豪华粽子制作数 | ✅ |
| `%duanwu_zongzi_total%` | 粽子总数（普通+豪华） | ✅ |
| `%duanwu_sign_days%` | 签到天数 | ✅ |
| `%duanwu_signed_today%` | 今日是否签到（yes/no） | |
| `%duanwu_fish_rewards%` | 钓鱼奖励次数 | ✅ |
| `%duanwu_shop_purchases%` | 商店兑换次数 | ✅ |
| `%duanwu_rice%` | 糯米背包数量 | |
| `%duanwu_leaf%` | 粽叶背包数量 | |
| `%duanwu_jujube%` | 红枣背包数量 | |
| `%duanwu_meat%` | 鲜肉背包数量 | |
| `%duanwu_materials_total%` | 材料总数（四项之和） | ✅ |

可在排行榜插件（如 LeaderHeads）中使用这些占位符。

---

## 8. 常见问题

### 8.1 插件没有启用怎么办？

检查控制台是否有：

```text
[DragonBoatFestival] DragonBoatFestival 已启用
```

如果没有，检查：

- 插件 jar 是否放在 `plugins` 目录
- 服务端是否为 Spigot/Paper 1.12.2
- Java 版本是否可运行 1.12.2 服务端
- 控制台是否有报错堆栈

---

### 8.2 玩家无法使用命令怎么办？

检查权限节点：

- 制作粽子：`duanwu.make`
- 打开商店：`duanwu.shop`
- 每日签到：`duanwu.sign`
- 查看指南：`duanwu.guide`
- 查看目标：`duanwu.goals`

默认情况下普通玩家拥有这些权限。

---

### 8.3 商店兑换没有给奖励怎么办？

检查 `shop.yml` 中的 `commands`。

例如：

```yaml
commands:
  - 'eco give {player} 1000'
```

如果服务器没有安装经济插件，`eco give` 命令可能无法执行。

---

### 8.4 为什么活动命令提示活动未开启？

检查 `config.yml`：

```yaml
festival:
  enabled: true
```

如果是 `false`，改为 `true` 后执行：

```text
/duanwu reload
```

---

### 8.5 PlaceholderAPI 占位符不生效怎么办？

1. 确认服务器已安装 PlaceholderAPI 插件。
2. 确认 PlaceholderAPI 版本 ≥ 2.11.5。
3. 重启服务器或 `/placeholderapi reload`。
4. 用 `/papi parse 玩家名 %duanwu_points%` 测试。

---

## 9. 快速测试流程

服主可以按下面流程测试插件：

1. 启动服务器，确认插件启用。
2. OP 执行：

   ```text
   /duanwu give 玩家名 rice 10
   /duanwu give 玩家名 leaf 5
   /duanwu give 玩家名 jujube 3
   /duanwu give 玩家名 meat 3
   ```

3. 玩家执行：

   ```text
   /duanwu materials
   /duanwu make normal
   /duanwu make luxury
   /duanwu points
   ```

4. 玩家执行：

   ```text
   /duanwu shop
   ```

5. 玩家执行：

   ```text
   /duanwu sign
   ```

6. OP 测试 BOSS：

   ```text
   /duanwu boss spawn
   ```

7. 玩家查看全服目标进度：

   ```text
   /duanwu goals
   ```

8. 测试 PlaceholderAPI（如已安装）：

   ```text
   /papi parse 玩家名 %duanwu_points%
   /papi parse 玩家名 %duanwu_zongzi_total%
   /papi parse 玩家名 %duanwu_materials_total%
   ```

---

## 10. 当前版本说明

当前版本 `1.0.0` 已实现以下功能：

- ✅ **材料收集** — 挖矿掉糯米、树叶掉粽叶、作物掉红枣、打怪掉糯米、杀动物掉鲜肉、钓鱼概率奖励
- ✅ **粽子制作** — 普通/豪华粽子，支持 GUI 和命令，物品可右键食用/交易
- ✅ **节日积分系统** — 独立积分，支持管理员增/改
- ✅ **活动商店** — 完全通过 `shop.yml` 自定义，支持任意材质/命令
- ✅ **每日签到** — 5 天循环签到，奖励可配置
- ✅ **幸运摸鱼** — 加权概率钓鱼奖励，支持幸运/传说礼包命令
- ✅ **BOSS 活动** — 可配置实体类型/血量/名称/奖励
- ✅ **全服目标系统** — 制作粽子/签到/BOSS 击杀共同推进
- ✅ **PlaceholderAPI 支持** — 13 个占位符，支持排行榜插件
- ✅ **Vault 经济接入** — 可选经济支持
- ✅ **数据持久化** — YAML 存储，线程安全延迟写入
- ✅ **配置热重载** — `/duanwu reload`
