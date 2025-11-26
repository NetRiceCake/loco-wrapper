# loco-wrapper

안드로이드 카카오톡 25.9.2 기반 비공식 카카오톡 클라이언트 (태블릿 서브 디바이스 로그인)

현재 오픈 채팅방에서만 작동합니다.

절대 본계정으로 돌려보지마세요.

## Example

![ex](./ex.png)

Main.java 파일 참고
```java
TalkClient client = new TalkClient(email, password, deviceName, deviceUuid, new TalkHandler() {
            @Override
            public void onMessage(Message msg) {
                if (msg.getType() != 1) return; // 1이 그냥 채팅, 그냥 채팅만 받기
                if (msg.getMessage().equals("!send")) {
                    getTalkClient().sendMessage(msg.getChatRoom(), "test");
                }
                else if (msg.getMessage().equals("!reply")) { // 답장
                    int replyType = 26; // 답장 타입
                    JsonObject extraObject = new JsonObject();
                    extraObject.addProperty("src_logId", msg.getLogId());
                    extraObject.addProperty("src_userId", msg.getAuthor().getId());
                    extraObject.addProperty("src_message", msg.getMessage());
                    extraObject.addProperty("src_type", msg.getType());
                    extraObject.addProperty("src_linkId", msg.getChatRoom().getLinkId());
                    getTalkClient().sendMessage(msg.getChatRoom(), replyType, "reply test", extraObject.toString());
                }
                else if (msg.getMessage().equals("!mention")) { // 멘션
                    JsonObject extraObject = new JsonObject();
                    JsonArray mentionArray = new JsonArray();
                    JsonObject mentionObject = new JsonObject();
                    mentionObject.addProperty("user_id", msg.getAuthor().getId());
                    JsonArray pos = new JsonArray();
                    pos.add(1);
                    mentionObject.add("at", pos);
                    mentionObject.addProperty("len", msg.getAuthor().getName().length());
                    mentionArray.add(mentionObject);
                    extraObject.add("mentions", mentionArray);
                    getTalkClient().sendMessage(msg.getChatRoom(), 1, "@" + msg.getAuthor().getName(), extraObject.toString());
                }
            }

            @Override
            public void onNewMember(ChatRoom room, Member member) {
                getTalkClient().sendMessage(room, member.getName() + "님이 들어왔습니다.");
            }

            @Override
            public void onDelMember(ChatRoom room, Member member) {
                getTalkClient().sendMessage(room, member.getName() + "님이 나갔습니다.");
            }
        });
client.connect();
```

## Usage

첫 로그인시에 기기등록이 필요합니다. 콘솔창에 방법 나오니 따라하세요.

로그인하면 로그인 정보(토큰 등)가 email_deviceName 폴더 안에 저장됩니다. 서버 연결이 안되면 삭제하고 시도하세요.

<U>**device uuid 무조건 바꾸시오.**</U>
