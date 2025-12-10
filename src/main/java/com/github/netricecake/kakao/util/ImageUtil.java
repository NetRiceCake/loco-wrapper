package com.github.netricecake.kakao.util;

import lombok.Getter;

public class ImageUtil {

    @Getter
    public static class ImageMeta {
        private boolean isValidJpeg;
        private int width;
        private int height;

        public ImageMeta(boolean isValidJpeg, int width, int height) {
            this.isValidJpeg = isValidJpeg;
            this.width = width;
            this.height = height;
        }
    }

    public static ImageMeta getImageMeta(byte[] data) {
        if (data == null || data.length < 4) {
            return new ImageMeta(false, 0, 0);
        }

        int i = 0;

        if ((data[i] & 0xFF) != 0xFF || (data[i + 1] & 0xFF) != 0xD8) {
            return new ImageMeta(false, 0, 0);
        }
        i += 2;

        while (i < data.length) {
            int marker;
            while ((marker = (data[i] & 0xFF)) != 0xFF) {
                i++;
                if (i >= data.length) return new ImageMeta(false, 0, 0);
            }

            do {
                marker = data[++i] & 0xFF;
            } while (marker == 0xFF);
            i++;
            if (isSOFMarker(marker)) {

                int height = ((data[i + 3] & 0xFF) << 8) | (data[i + 4] & 0xFF);
                int width = ((data[i + 5] & 0xFF) << 8) | (data[i + 6] & 0xFF);

                return new ImageMeta(true, width, height);
            }

            int length = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            i += length;
        }

        return new ImageMeta(false, 0, 0);
    }

    private static boolean isSOFMarker(int marker) {
        return marker >= 0xC0 && marker <= 0xCF
                && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
    }

}
