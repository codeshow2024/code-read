package top.codeshow;

import top.codeshow.util.MyStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MyHashMap<K, V> {
    public static void main(String[] args) {
        System.out.println(1);
        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        System.out.println(map);
        MyHashMap<Integer, Integer> myMap = new MyHashMap<>();
        myMap.put(1, 1);
        myMap.put(3, 3);
        myMap.put(4, 4);
        myMap.put(4, 5);
        System.out.println(myMap);
    }

    // 默认负载因子
    static final float LOAD_FACTOR = 0.75f;
    final float loadFactor;
    transient Node<K, V>[] table;
    // 要调整下一个值（容量*负载因子）
    int threshold;
    // 最大容量
    static final int MAXIMUM_CAPACITY = 1 << 30;
    // 默认最小容量
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    transient int modCount;
    // 容量
    transient int size;

    public MyHashMap() {
        this.loadFactor = LOAD_FACTOR;
    }

    static final int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    public V put(K key, V value) {
        return putVal(hash(key), key, value, false);
    }

    public V putVal(int hash, K key, V value, boolean onlyIfAbsent) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, i;
        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        if ((p = tab[i = (n - 1) & hash]) == null) {
            tab[i] = new Node<>(hash, key, value, null);
        } else {
            Node<K, V> e;
            K k;
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
            } else {
                for (int binCount = 0; ; binCount++) {
                    if ((e = p.next) == null) {
                        p.next = new Node<>(hash, key, value, null);
                        break;
                    }
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    p = e;
                }
            }
            // e 不为空，说明之前存在这个 key
            if (e != null) {
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }
                return oldValue;
            }
        }
        // 到达这一步表示数据是插入的，而不是替换
        ++modCount;
        if (++size >= threshold) {
            resize();
        }
        return null;
    }

    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap = 0, newThr = 0;
        if (oldCap == 0 && oldThr == 0) {
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (loadFactor * DEFAULT_INITIAL_CAPACITY);
        }
        threshold = newThr;
        Node<K, V>[] newTab = new Node[newCap];
        table = newTab;
        return newTab;
    }

    @Override
    public String toString() {
        if (table == null) {
            return "{}";
        }
        List<String> list = new ArrayList<>();
        for (Node<K, V> node : table) {
            Node<K, V> p = node;
            while (p != null) {
                list.add(p.key + "=" + p.value);
                p = p.next;
            }
        }
        return "{" + MyStringUtils.join(list, ", ") + "}";
    }

    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            return obj instanceof Node<?, ?> e
                    && Objects.equals(e.getKey(), key)
                    && Objects.equals(e.getValue(), value);
        }
    }
}
