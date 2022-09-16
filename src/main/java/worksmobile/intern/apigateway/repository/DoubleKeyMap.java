package worksmobile.intern.apigateway.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DoubleKeyMap<K1, K2> {
    private K1 firstKey;
    private K2 secondKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DoubleKeyMap key = (DoubleKeyMap) o;

        if (firstKey != null ? !firstKey.equals(key.getFirstKey()) : key.getFirstKey() != null) {
            return false;
        }

        if (secondKey != null ? !secondKey.equals(key.getSecondKey()) : key.getSecondKey() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstKey != null ? firstKey.hashCode() : 0;
        result = 31 * result + (secondKey != null ? secondKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" + firstKey + ", " + secondKey + "]";
    }

}
