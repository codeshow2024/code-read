package top.codeshow;

import top.codeshow.util.MyStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 简易实现 HashMap
 * （未实现节点数目达到一定数目，链表-->红黑树）
 *
 * @param <K> key 类型
 * @param <V> value 类型
 */
public class MyHashMap<K, V> {
    public static void main(String[] args) {
        MyHashMap<Integer, Integer> myMap = new MyHashMap<>();
        for (int i = 0; i < 1000; i++) {
            myMap.put(i, i);
        }
        Integer put = myMap.put(4, 5);
        System.out.println(put);
        System.out.println(myMap);
        System.out.println(myMap.get(4));
        System.out.println(myMap);
        System.out.println(myMap.remove(4));
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
    // 容量
    transient int size;

    public MyHashMap() {
        this.loadFactor = LOAD_FACTOR;
    }

    static int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    public V put(K key, V value) {
        return putVal(hash(key), key, value, false);
    }

    static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap);
        return n < 0 ? 1 : (n >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : n + 1);
    }

    public V remove(V key) {
        Node<K, V>[] tab;
        Node<K, V> e;
        int n, i;
        if ((tab = table) != null &&
                (n = tab.length) > 0 &&
                (e = tab[(i = hash(key) & (n - 1))]) != null) {
            if (e.hash == hash(key) && Objects.equals(e.key, key)) {
                V value = e.value;
                tab[i] = e.next;
                return value;
            } else {
                while (e.next != null) {
                    if (e.next.hash == hash(key) && Objects.equals(e.next.key, key)) {
                        V value = e.next.value;
                        e.next = e.next.next;
                        return value;
                    }
                }
            }
        }
        return null;
    }

    public V get(K key) {
        if (table == null || table.length == 0) {
            return null;
        }
        int n = table.length;
        int hash = hash(key);
        int i = hash & (n - 1);
        Node<K, V> e = table[i];
        while (e != null) {
            if (e.hash == hash && Objects.equals(e.key, key)) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true);
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
                for (; ; ) {
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
        } else if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) {
                newThr = oldThr << 1;
            }
        }
        threshold = newThr;
        @SuppressWarnings({"unchecked"}) Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; j++) {
                Node<K, V> e = oldTab[j];
                if (e != null) {
                    if (e.next == null) {
                        newTab[e.hash & (newCap - 1)] = e;
                    } else {
                        // 做两个链表，索引位置分别为 j/j+oldCap
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {
                            // 索引位置不变
                            if ((e.hash & oldCap) == 0) {
                                if (loHead == null) {
                                    loHead = e;
                                }
                                if (loTail != null) {
                                    loTail.next = e;
                                }
                                loTail = e;
                            }
                            // 索引位置 oldCap+j
                            else {
                                if (hiHead == null) {
                                    hiHead = e;
                                }
                                if (hiTail != null) {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                            e = e.next;
                        } while (e.next != null);
                        if (loHead != null) {
                            loTail.next = null;
                            newTab[j] = loTail;
                        }
                        if (hiHead != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
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
            return obj instanceof Node<?, ?> e && Objects.equals(e.getKey(), key) && Objects.equals(e.getValue(), value);
        }
    }
}
