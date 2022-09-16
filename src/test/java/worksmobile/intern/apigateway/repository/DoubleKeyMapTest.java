package worksmobile.intern.apigateway.repository;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleKeyMapTest {

    @Test
    public void 더블_키_생성_테스트() {
        // given
        DoubleKeyMap<String, String> key = new DoubleKeyMap<>("key1", "key2");
        // when
        String foundFirstKey = key.getFirstKey();
        // then
        assertThat(foundFirstKey).isEqualTo("key1");
    }

    @Test
    public void 더블_키_비교_테스트() {
        // given
        DoubleKeyMap<String, String> key1 = new DoubleKeyMap<>("key1", "key2");
        DoubleKeyMap<String, String> key2 = new DoubleKeyMap<>("key1", "key2");
        // when
        boolean isKeySame = key1.equals(key2);
        // then
        assertThat(isKeySame).isEqualTo(true);
    }

    @Test
    public void 더블_키_맵_확인() {
        // given
        DoubleKeyMap<String, String> key1 = new DoubleKeyMap<>("GET", "/blog");
        DoubleKeyMap<String, String> key2 = new DoubleKeyMap<>("GET", "/blogs/{blogId}");

        Map<DoubleKeyMap, String> mapInfo = new HashMap<>();
        mapInfo.put(key1, "getBlog");
        mapInfo.put(key2, "getSpecificBlog");
        // when
        String res = mapInfo.get(key1);
        // then
        assertThat(res).isEqualTo("getBlog");
        assertThat(mapInfo).hasSize(2);
    }
}