# DragonBoatFestival 用户 Wiki

DragonBoatFestival 是一个适用于 Spigot/Paper 1.12.2 的端午节活动插件。玩家可以通过日常行为收集粽子材料、制作粽子获得节日积分，并使用积分在活动商店兑换奖励。

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
   └── playerdata/
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

它会打开“端午活动中心”GUI，里面可以直接进入：

- 我的活动状态
- 制作粽子
- 活动商店
- 每日签到
- 龙舟竞速
- 幸运摸鱼说明
- 新手指南

如果玩家不知道下一步做什么，可以输入：

```text
/duanwu status
```

插件会告诉玩家当前积分、材料、是否签到、龙舟竞速状态，并给出下一步推荐。

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

玩家刚进服时，如果活动开启，插件也会自动发送一段小白引导。

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
1 节日积分
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
5 节日积分
```

制作时会从玩家背包中扣除对应端午材料实体物品。普通原版物品不会被消耗。

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

默认商品：

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

每天只能签到一次。签到奖励由 `config.yml` 中的 `sign-rewards` 配置决定。

默认签到奖励包含：

- 金币命令奖励
- 节日积分
- 幸运钥匙
- 经验
- 标题奖励

插件会记录玩家当天是否已签到。

---

### 2.10 龙舟竞速

玩家参加竞速：

```text
/duanwu race join
```

查看排行榜：

```text
/duanwu race top
```

说明：

1. 管理员需要先设置起点和终点。
2. 玩家执行 `/duanwu race join` 后开始计时。
3. 玩家到达终点附近时自动完成计时。
4. 插件会记录玩家最好成绩。
5. `/duanwu race top` 显示前 10 名排行榜。

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
- 商店菜单
- 龙舟竞速数据

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

该命令会把对应端午材料实体物品放入玩家背包；背包已满时，多出的物品会掉落在玩家当前位置附近。

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

### 3.6 设置龙舟赛起点

站在起点位置执行：

```text
/duanwu race create start
```

也可以执行：

```text
/duanwu race create
```

不填写 `start` 或 `end` 时，默认设置起点。

---

### 3.7 设置龙舟赛终点

站在终点位置执行：

```text
/duanwu race create end
```

---

### 3.8 删除龙舟赛道和记录

```text
/duanwu race delete
```

该命令会清空：

- 起点
- 终点
- 当前参赛状态
- 排行榜记录

---

## 4. 权限节点

| 权限 | 默认 | 说明 |
| --- | --- | --- |
| `duanwu.admin` | OP | 管理员命令 |
| `duanwu.sign` | 所有玩家 | 每日签到 |
| `duanwu.shop` | 所有玩家 | 打开活动商店 |
| `duanwu.make` | 所有玩家 | 制作粽子 |
| `duanwu.race` | 所有玩家 | 参加龙舟竞速 |

---

## 5. 配置文件说明

### 5.1 `config.yml`

#### 活动开关

```yaml
festival:
  enabled: true
```

设置为 `false` 后，除 `/duanwu reload` 外，大部分活动命令和掉落逻辑会停止。

---

#### 自动保存间隔

```yaml
auto-save-seconds: 300
```

单位：秒。

插件会定时保存在线玩家数据。实际最小值为 60 秒。

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
  rice: 5
  leaf: 5
  jujube: 3
  meat: 3
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

#### 签到奖励

```yaml
sign-rewards:
  day1:
    message: '&a签到成功，获得金币奖励。'
    commands:
      - 'eco give {player} 100'
```

支持字段：

| 字段 | 说明 |
| --- | --- |
| `message` | 签到成功提示 |
| `points` | 增加节日积分 |
| `exp` | 增加经验 |
| `commands` | 控制台执行命令列表 |

---

### 5.2 `messages.yml`

用于修改插件提示文本。

颜色代码使用 `&`，例如：

```yaml
prefix: '&a[端午节]&r '
no-permission: '&c你没有权限执行该命令。'
```

---

### 5.3 `shop.yml`

用于配置活动商店商品。

示例：

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
| `name` | 商品显示名 |
| `material` | GUI 中显示的物品材质 |
| `points` | 兑换需要的积分 |
| `lore` | 商品介绍 |
| `commands` | 兑换成功后由控制台执行的命令 |

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
- 最近签到日期

端午材料是玩家背包中的实体物品，不再保存在玩家数据文件中。

龙舟竞速数据保存在：

```text
plugins/DragonBoatFestival/race.yml
```

保存内容包括：

- 起点位置
- 终点位置
- 玩家最好成绩

---

## 7. 常见问题

### 7.1 插件没有启用怎么办？

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

### 7.2 玩家无法使用命令怎么办？

检查权限节点：

- 制作粽子：`duanwu.make`
- 打开商店：`duanwu.shop`
- 每日签到：`duanwu.sign`
- 龙舟竞速：`duanwu.race`

默认情况下普通玩家拥有这些权限。

---

### 7.3 商店兑换没有给奖励怎么办？

检查 `shop.yml` 中的 `commands`。

例如：

```yaml
commands:
  - 'eco give {player} 1000'
```

如果服务器没有安装经济插件，`eco give` 命令可能无法执行。

---

### 7.4 为什么龙舟竞速不能加入？

如果提示赛道尚未设置完成，需要管理员先设置：

```text
/duanwu race create start
/duanwu race create end
```

---

### 7.5 为什么活动命令提示活动未开启？

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

## 8. 快速测试流程

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

6. OP 设置龙舟赛道：

   ```text
   /duanwu race create start
   /duanwu race create end
   ```

7. 玩家测试竞速：

   ```text
   /duanwu race join
   /duanwu race top
   ```

---

## 9. 当前版本说明

当前版本已经实现基础端午节活动玩法，包括：

- 材料收集
- 粽子制作
- 节日积分
- 活动商店
- 每日签到
- 钓鱼奖励
- 龙舟竞速
- 玩家数据保存
- 配置热重载

当前版本未实际接入 Vault、PlayerPoints、PlaceholderAPI。`plugin.yml` 中保留了软依赖声明，但插件核心玩法可以独立运行。
