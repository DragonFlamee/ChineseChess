# 中国象棋联机对战系统 (Chinese Chess Online)

这是一个基于 Java Swing 开发的中国象棋联机对战系统。系统采用 **客户端-服务器 (C/S)** 架构，通过 Socket 编程实现两名玩家的实时对弈、胜负判定及即时聊天功能。

## 1. 项目结构与核心类

### 核心逻辑 (Core Logic)

* **ChessPiece.java**: 抽象基类，定义棋子的颜色、类型、坐标及通用绘制方法。
* **各棋子实现类**: 包含 `General` (将), `Advisor` (士), `Elephant` (象), `Chariot` (车), `Cannon` (炮) 等，重写了 `moveLogic` 走子逻辑。

### 网络与界面 (Networking & UI)

* **ChessServer.java**: 服务端。负责管理连接、分配阵营并中转消息。
* **ChessClient.java**: 客户端网络模块。封装了 Socket 通信及消息解析接口。
* **GamePanel.java**: 核心 UI。处理用户点击、渲染棋盘、同步远程走子。
* **GameMain.java**: 程序的启动入口。

---

## 2. 启动方式详解 (详细步骤)

本系统必须按顺序启动，确保两个客户端连接到同一服务端。

### 第一步：启动服务器 (Server)

1. **运行类**：`ChessServer.java`。
2. **逻辑**：默认监听 **12345** 端口，等待两名玩家接入。
3. **现象**：控制台输出 “服务器启动，等待玩家连接...”。

### 第二步：启动客户端 A (红方)

1. **运行类**：`GameMain.java`。
2. **角色**：首个连接的玩家被服务端自动分配为 `Side.RED`（红方）。
3. **状态**：窗口弹出，并提示“等待另一位玩家加入...”。

### 第三步：启动客户端 B (黑方)

1. **运行类**：再次启动 `GameMain.java`。
2. **角色**：第二个连接者分配为 `Side.BLACK`（黑方）。
3. **开始**：当两名玩家就位，服务端发送 `START` 指令，双端弹出“游戏开始！”提示。

---

## 3. 网络通信协议与数据格式

系统通过 `BufferedReader` 和 `PrintWriter` 进行基于文本行的实时指令传输。

### 3.1 核心数据发送函数

#### 1. 棋子移动指令 (`sendMove`)

当玩家本地走子通过逻辑校验后，客户端调用此函数：

* **代码位置**：`ChessClient.java`
* **函数签名**：`public void sendMove(int fromRow, int fromCol, int toRow, int toCol)`
* **数据形式**：`"7,1,9,2"` (代表起始点与终点的坐标)
* **处理流程**：客户端 A 发送坐标 -> 服务端中转 -> 客户端 B 调用 `handleRemoteMove` 更新画面。

#### 2. 聊天指令 (`sendChatMessage`)

支持对弈过程中的即时文字交流：

* **函数签名**：`public void sendChatMessage(String message)`
* **数据形式**：`"CHAT:消息内容"`
* **转发格式**：服务端接收后附带发送者身份转发，例如 `"CHAT:RED:你好！"`。

#### 3. 游戏状态指令

| 指令前缀 | 形式示例 | 说明 |
| --- | --- | --- |
| `SIDE:` | `SIDE:RED` | 服务器分配阵营，由 `onSideAssigned` 回调处理。 |
| `START` | `START` | 触发 `onGameStart`，激活本地操作权限。 |
| `WIN:` | `WIN:RED` | 当对方将/帅被吃掉时，发送胜负消息。 |

---

## 4. 技术特性与常见问题

* **异步接收**：`ChessClient` 在独立线程中循环读取数据 (`in.readLine()`)，确保接收网络消息时不会导致游戏界面卡死。
* **UI 线程同步**：所有网络消息触发的 UI 变更（如对方走子、添加聊天气泡）均包裹在 `SwingUtilities.invokeLater` 中执行，保证 Swing 组件的线程安全。
* **逻辑校验**：在发送数据前，本地会先调用棋子的 `moveLogic`。只有当返回 `true` 时，才会发送网络封包并切换回合（`isMyTurn = false`）。
* **常见问题**：
* **连接失败**：请确保服务器已先启动。
* **无法行棋**：系统严格遵守回合制，只有在 `isMyTurn` 为 `true` 且棋子属于己方阵营时才能移动。

## 5. 数据库持久化实现 (Database Implementation)

系统集成了 MySQL 数据库，用于记录每一场对局的元数据及详细步数，支持复盘数据查询与对局审计。

### 5.1 数据库结构

系统包含两张核心表：`games`（对局主表）和 `moves`（走子明细表），通过外键 `game_id` 进行关联。

* **games 表**：存储对局基本信息。
* `id`: 自增主键，唯一标识一场对局。
* `player_red` / `player_black`: 记录红黑双方玩家名称。
* `start_time`: 自动记录对局创建时间。


* **moves 表**：记录每一回合的操作细节。
* `game_id`: 关联 `games` 表的主键。
* `side`: 记录该步是 RED 还是 BLACK 走子。
* `piece_name`: 棋子类型（如 CHARIOT, GENERAL）。
* `from_row, from_col, to_row, to_col`: 移动的前后坐标。



### 5.2 核心数据管理类 (`DatabaseManager`)

该类封装了所有 JDBC 操作，主要包含以下功能：

1. **对局初始化 (`createNewGame`)**：
* 使用 `Statement.RETURN_GENERATED_KEYS` 在插入对局后获取数据库生成的自增 `id`。
* 该 ID 将作为当前对局的唯一句柄。


2. **异步走子记录 (`saveMove`)**：
* 为了防止数据库写入操作（I/O 阻塞）导致游戏画面卡顿，系统开启了**独立线程**进行异步插入。
* 每次本地玩家行棋成功后，自动向 `moves` 表追加一条记录。



### 5.3 联机同步与一致性保障

在复杂的联机环境下，系统采取了以下策略确保数据准确：

* **唯一标识同步**：
为了避免两名玩家各自生成不同的 `game_id`，系统规定**仅由红方（先手）创建数据库记录**。创建成功后，红方通过自定义网络协议消息（如 `SYSTEM_GAME_ID:123`）将 ID 广播给黑方。
* **单点写入原则**：
联机对战时，只有**当前回合的走子方**负责执行 `saveMove`。这样既能保证每步棋都被记录，又避免了双方重复写入同一条数据。
* **本地模式支持**：
当检测到服务端未启动进入“单机模式”时，`GamePanel` 会自动触发本地对局创建逻辑，确保离线对弈同样能被记录。

### 5.4 维护常用指令

* **重置数据库**（清空并让 ID 从 1 开始）：
```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE moves;
TRUNCATE TABLE games;
SET FOREIGN_KEY_CHECKS = 1;

```



---
