# 🎋 DragonBoatFestival

端午节活动插件 — Spigot/Paper 1.12.2

## 功能简介

玩家通过日常行为收集粽子材料（糯米、粽叶、红枣、鲜肉），制作普通/豪华粽子获得节日积分，使用积分在活动商店兑换奖励。

- ✅ **材料收集** — 挖矿、打怪、破坏树叶、采集作物、钓鱼均可获得端午材料
- ✅ **粽子制作** — 普通/豪华粽子，支持 GUI 和命令制作，物品可右键食用或交易
- ✅ **节日积分系统** — 独立积分，支持管理员增/改
- ✅ **活动商店** — 通过 `shop.yml` 完全自定义商品和奖励命令
- ✅ **每日签到** — 5 天循环签到，奖励可配置
- ✅ **幸运摸鱼** — 钓鱼概率奖励，支持幸运/传说礼包
- ✅ **BOSS 活动** — 可配置实体类型/血量/名称/奖励
- ✅ **全服目标系统** — 全服玩家共同推进任务
- ✅ **PlaceholderAPI 支持** — 13 个占位符，支持排行榜插件
- ✅ **Vault 经济接入** — 可选经济支持
- ✅ **TrMenu 支持** — 可选菜单界面
- ✅ **配置热重载** — `/duanwu reload`

## 快速开始

1. 将 `DragonBoatFestival-1.0.0.jar` 放入 `plugins/` 目录
2. 重启服务器
3. 玩家输入 `/duanwu` 打开活动中心

## 命令

| 命令 | 说明 |
| --- | --- |
| `/duanwu` | 打开活动中心 |
| `/duanwu help` | 查看帮助 |
| `/duanwu guide` | 新手指南 |
| `/duanwu status` | 查看我的状态 |
| `/duanwu points` | 查看积分 |
| `/duanwu materials` | 查看材料 |
| `/duanwu make [normal\|luxury]` | 制作粽子 |
| `/duanwu shop` | 活动商店 |
| `/duanwu sign` | 每日签到 |
| `/duanwu fish` | 幸运摸鱼说明 |
| `/duanwu goals` | 查看全服任务 |
| `/duanwu boss spawn` | 召唤 BOSS（管理员） |
| `/duanwu reload` | 重载配置（管理员） |
| `/duanwu give <玩家> <材料> <数量>` | 给予材料（管理员） |
| `/duanwu addpoint/setpoint <玩家> <数量>` | 增/改积分（管理员） |

## 配置

- `config.yml` — 主配置（材料掉落、制作消耗、签到奖励、BOSS、全服目标等）
- `messages.yml` — 消息文案
- `shop.yml` — 商店商品

## 数据存储

```
plugins/DragonBoatFestival/
├── config.yml
├── messages.yml
├── shop.yml
├── goals.yml          # 全服目标进度
└── playerdata/
    └── UUID.yml       # 玩家数据
```

## 占位符

| 占位符 | 说明 |
| --- | --- |
| `%duanwu_points%` | 节日积分 |
| `%duanwu_normal_made%` | 普通粽子制作数 |
| `%duanwu_luxury_made%` | 豪华粽子制作数 |
| `%duanwu_zongzi_total%` | 粽子总数 |
| `%duanwu_sign_days%` | 签到天数 |
| `%duanwu_signed_today%` | 今日是否签到 |
| `%duanwu_fish_rewards%` | 钓鱼奖励次数 |
| `%duanwu_shop_purchases%` | 商店兑换次数 |
| `%duanwu_rice/leaf/jujube/meat%` | 材料背包数量 |
| `%duanwu_materials_total%` | 材料总数 |
