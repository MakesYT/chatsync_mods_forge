# chatsync_mods_forge 1.12.2/1.16.5/1.19.2

![image](https://user-images.githubusercontent.com/42534870/225544104-319af1df-fcd4-410e-a280-da66d39081bb.png)

## 主项目页面 https://github.com/MakesYT/chatsync

## 面向开发者的mod规范  
### (一般情况你只需要使用chatsync的三件套即可完成全流程的图片转发以及显示)
在开始前你需要了解的事  
1. 所有来自客户端的图片不会由某个客户端直接向其它客户端发送而是经过服务器转发,所以你可以对图片进行任何操作
1. 图片以base64编码传输
1. MC对于数据包是有大小限制的  
1. string转byte[]请使用UTF-8编码
1. 严格注意数据包的ImgID不能重复
1. 图片发送完之后客户端不会直接显示,而是需要服务器发送一条聊天消息,格式为[ImgID=imgId],仅包含就行,不用只有格式的消息  


在ID为"chatimg:img"上创建接收和发送的通道,如果必要IDX请设置为6969

### 下面是Bukkit插件的示例

 ```java
String channel = "chatimg:img";
getServer().getMessenger().registerIncomingPluginChannel(this, channel,
                (channel, player, message) -> {
                   //处理来自mod的数据包,主要是客户端的图片发送
                });//创建接收通道
getServer().getMessenger().registerOutgoingPluginChannel(this, channel);//创建发送通道

private static void send(Player player, String msg) {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer(bytes.length + 1);
        buf.writeByte(IDX);
        buf.writeBytes(bytes);
        player.sendPluginMessage(this, channel, buf.array());
}//调用此方法向客户端发送数据包
```
### 数据包规范
### 注意来自客户端和发送到客户端数据包是不同的,但相差不大
### 严格注意数据包的ImgID不能重复
```
来自客户端的数据包
{"id":"c3e44613-4635-4df9-9f2e-6c9b331a97d4","base64imgdata":"base64imgdata","packageNum":338,"index":0,"data":"******","sender":"Dev"}
发送到客户端的数据包
{"id":"1","base64imgdata":"base64imgdata","packageNum":338,"index":0,"data":"******"}
```
均为Json数据包以String传输  
你可以看出二者区别只是一个sender(当然id有一定区别)  
下表中(服务器)表示发送到客户端,(客户端)是客户端发送到服务器  
|  名称   | 值  | 用处 |
|  ----  | ----  | ---- |
| id  | int(服务器)或者(String)UUID(客户端) | 用于标识图片(严格注意不能重复) |
| base64imgdata  | base64imgdata | 仅数据包表示 |
|  packageNum  | int  | 总计多少个数据包 |
|  index  | int  | 当前数据包序数 |
|  data  | String  | 数据包内容(base64编码后的图片分段数据) |
|  sender  | String  | 图片发送者(仅客户端) |
### 下面是Bukkit发送图片并展示的一个示例,该示例使用了Alibaba的fastJson
```java

        int ImgID = 0;//服务器应确保每一个发送到客户端的ID是唯一的
        String base64="...."//图片base64编码后的数据
        new BukkitRunnable() {
            @Override
            public void run() {
                int imgId = ImgID++;
                JSONObject json = new JSONObject();
                json.put("id", imgId);
                json.put("base64imgdata", "base64imgdata");
                int length = 1024 * 30;
                int n = (base64.length() + length - 1) / length; 
                json.put("packageNum", n);
                String[] split = new String[n];
                for (int i = 0; i < n; i++) {
                    if (i < (n - 1)) {
                        split[i] = base64.substring(i * length, (i + 1) * length);
                    } else {
                        split[i] = base64.substring(i * length);
                    }
                }//对图片的base64数据拆分
                for (int i = 0; i < split.length; i++) {
                    JSONObject temp = json;
                    temp.put("index", i);
                    temp.put("data", split[i]);
                    for (Object player1 : player) {
                        send((Player) player1, temp.toJSONString());
                    }//向每一个玩家发送数据包
                }
                    for (Object player1 : player) {
                        ((Player) player1).sendMessage(sender + "[ImgID=" + imgId + "]");
                    }//注意:在发送完数据包后,向玩家发送一条消息以展示此图片
                }
            }.runTaskAsynchronously(this);//注意在新的线程处理,否则可能造成严重卡顿
```
