package com.github.netricecake.kakao.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;

import java.nio.ByteBuffer;

public class BsonUtil {

    private final static BsonDocumentCodec bsonDocumentCodec = new BsonDocumentCodec();
    private final static Gson gson = new Gson();

    public static byte[] jsonToBson(String json) {
        RawBsonDocument rawBson = RawBsonDocument.parse(json);
        ByteBuffer buffer = rawBson.getByteBuffer().asNIO();
        byte[] exactBytes = new byte[buffer.remaining()];
        buffer.get(exactBytes);
        return exactBytes;
    }

    public static byte[] jsonObjectToBson(JsonObject jsonObject) {
        return jsonToBson(gson.toJson(jsonObject));
    }

    public static String bsonToJson(byte[] bson) {
        BsonDocument doc = bsonDocumentCodec.decode(
                new BsonBinaryReader(ByteBuffer.wrap(bson)),
                DecoderContext.builder().build()
        );
        return doc.toString();
    }

    public static JsonObject bsonToJsonObject(byte[] bson) {
        return JsonParser.parseString(bsonToJson(bson)).getAsJsonObject();
    }

}
