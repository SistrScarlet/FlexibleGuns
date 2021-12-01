package net.sistr.flexibleguns.wip.ecs.system

import net.sistr.flexibleguns.wip.ecs.component.IComponentHolder

interface ISystem {
    fun run()

    fun <T> iterate(
        a: IComponentHolder<T>,
        consumer: (Int, T) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                consumer.invoke(id, it.data)
            }
    }

    fun <T, U> iterate(
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        consumer: (Int, T, U) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                b.get(id).ifPresent { bD ->
                    consumer.invoke(id, it.data, bD.data)
                }
            }
    }

    fun <T, U, V> iterate(
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        c: IComponentHolder<V>,
        consumer: (Int, T, U, V) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                b.get(id).ifPresent { bD ->
                    c.get(id).ifPresent { cD ->
                        consumer.invoke(id, it.data, bD.data, cD.data)
                    }
                }
            }
    }

    fun <T, U, V, W> iterate(
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        c: IComponentHolder<V>,
        d: IComponentHolder<W>,
        consumer: (Int, T, U, V, W) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                b.get(id).ifPresent { bD ->
                    c.get(id).ifPresent { cD ->
                        d.get(id).ifPresent { dD ->
                            consumer.invoke(id, it.data, bD.data, cD.data, dD.data)
                        }
                    }
                }
            }
    }

    fun <T, U, V, W, X> iterate(
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        c: IComponentHolder<V>,
        d: IComponentHolder<W>,
        e: IComponentHolder<X>,
        consumer: (Int, T, U, V, W, X) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                b.get(id).ifPresent { bD ->
                    c.get(id).ifPresent { cD ->
                        d.get(id).ifPresent { dD ->
                            e.get(id).ifPresent { eD ->
                                consumer.invoke(id, it.data, bD.data, cD.data, dD.data, eD.data)
                            }
                        }
                    }
                }
            }
    }

    fun <T, U, V, W, X, Y> iterate(
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        c: IComponentHolder<V>,
        d: IComponentHolder<W>,
        e: IComponentHolder<X>,
        f: IComponentHolder<Y>,
        consumer: (Int, T, U, V, W, X, Y) -> Unit
    ) {
        a.getComponents()
            .forEach {
                val id = it.id
                b.get(id).ifPresent { bD ->
                    c.get(id).ifPresent { cD ->
                        d.get(id).ifPresent { dD ->
                            e.get(id).ifPresent { eD ->
                                f.get(id).ifPresent { fD ->
                                    consumer.invoke(id, it.data, bD.data, cD.data, dD.data, eD.data, fD.data)
                                }
                            }
                        }
                    }
                }
            }
    }

}