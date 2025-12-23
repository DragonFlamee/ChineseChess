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



---
