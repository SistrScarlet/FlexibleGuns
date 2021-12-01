package net.sistr.flexibleguns.wip.ecs.event

import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.wip.ecs.component.ComponentHolders
import net.sistr.flexibleguns.wip.ecs.component.IComponentHolder
import net.sistr.flexibleguns.util.Input
import net.sistr.flexibleguns.util.Inputable

object Events {
    val ITEM_TICK_EVENT = Event<EventContext>()
    val ITEM_HOLD_EVENT = Event<EventContext>()
    val ITEM_ON_HOLD_EVENT = Event<EventContext>()
    val ITEM_UN_HOLD_EVENT = Event<EventContext>()
    val ITEM_USE_EVENT = Event<EventContext>()
    val GUN_TRIGGER_EVENT = Event<EventContext>()
    val GUN_CAN_SHOOT_EVENT = CancelableEvent<CancelableEvent.CancelableContext>()
    val GUN_SHOOT_EVENT = Event<EventContext>()

    fun init() {
        ITEM_TICK_EVENT.register(Identifier(FlexibleGunsMod.MODID, "gun_delay"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    ComponentHolders.ITEM_SHOOT_DELAY_HOLDER.get(ctx.getEntityId())
                        .ifPresent {
                            if (0 < it.data.delay) {
                                //todo Eventで減少量の指定
                                it.data.delay--
                            }
                        }
                }
            }
        )
        ITEM_TICK_EVENT.register(Identifier(FlexibleGunsMod.MODID, "gun_burst_delay"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    ComponentHolders.ITEM_BURSTABLE_HOLDER.get(ctx.getEntityId())
                        .ifPresent {
                            if (0 < it.data.burstDelay) {
                                //todo Eventで減少量の指定
                                it.data.burstDelay--
                            }
                        }
                }
            }
        )
        ITEM_TICK_EVENT.register(Identifier(FlexibleGunsMod.MODID, "item_hold"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    iterate(
                        ctx.getEntityId(),
                        ComponentHolders.ITEM_COMPONENT_HOLDER,
                        ComponentHolders.ITEM_HOLD_HOLDER
                    ) { _, itemComponent, itemHold ->
                        val stack = itemComponent.stack
                        val holder = itemComponent.holder
                        val heldHand = when {
                            (stack == holder.mainHandStack) -> Hand.MAIN_HAND
                            (stack == holder.offHandStack) -> Hand.OFF_HAND
                            else -> null
                        }
                        itemHold.heldHand = heldHand
                        val hold = heldHand != null
                        itemHold.hold = hold
                        val prevHold = itemHold.prevHold
                        if (hold && !prevHold) {
                            ITEM_ON_HOLD_EVENT.runEvent(ctx)
                        } else if (!hold && prevHold) {
                            ITEM_UN_HOLD_EVENT.runEvent(ctx)
                        }
                        if (hold) {
                            ITEM_HOLD_EVENT.runEvent(ctx)
                        }
                        itemHold.prevHold = hold
                    }
                }
            }
        )
        ITEM_HOLD_EVENT.register(Identifier(FlexibleGunsMod.MODID, "item_use"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    iterate(
                        ctx.getEntityId(),
                        ComponentHolders.ITEM_COMPONENT_HOLDER,
                        ComponentHolders.ITEM_HOLD_HOLDER
                    ) { _, itemComponent, _ ->
                        val holder = itemComponent.holder
                        if ((holder as Inputable).getInputKeyFG(Input.FIRE)) {
                            ITEM_USE_EVENT.runEvent(ctx)
                        }
                    }
                }
            }
        )
        ITEM_USE_EVENT.register(Identifier(FlexibleGunsMod.MODID, "gun_trigger"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    iterate(
                        ctx.getEntityId(),
                        ComponentHolders.ITEM_COMPONENT_HOLDER,
                        ComponentHolders.ITEM_HOLD_HOLDER,
                        ComponentHolders.ITEM_SHOOTABLE_HOLDER
                    ) { _, _, _, _ ->
                        GUN_TRIGGER_EVENT.runEvent(ctx)
                    }
                }
            }
        )
        GUN_TRIGGER_EVENT.register(Identifier(FlexibleGunsMod.MODID, "gun_shoot"),
            object : IEvent<EventContext> {
                override fun runEvent(ctx: EventContext) {
                    iterate(
                        ctx.getEntityId(),
                        ComponentHolders.ITEM_COMPONENT_HOLDER,
                        ComponentHolders.ITEM_HOLD_HOLDER,
                        ComponentHolders.ITEM_SHOOTABLE_HOLDER
                    ) { _, _, _, _ ->
                        val ctxC = CancelableEvent.CancelableContext(ctx.getEntityId(), false)
                        GUN_CAN_SHOOT_EVENT.runEvent(ctxC)
                        if (!ctxC.cancel) {
                            GUN_SHOOT_EVENT.runEvent(ctx)
                        }
                    }
                }
            }
        )

    }

    fun <T> iterate(
        id: Int,
        a: IComponentHolder<T>,
        consumer: (Int, T) -> Unit
    ) {
        a.get(id).ifPresent { (i, aD) ->
            consumer.invoke(i, aD)
        }
    }

    fun <T, U> iterate(
        id: Int,
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        consumer: (Int, T, U) -> Unit
    ) {
        a.get(id).ifPresent { (_, aD) ->
            b.get(id).ifPresent { (i, bD) ->
                consumer.invoke(i, aD, bD)
            }
        }
    }

    fun <T, U, V> iterate(
        id: Int,
        a: IComponentHolder<T>,
        b: IComponentHolder<U>,
        c: IComponentHolder<V>,
        consumer: (Int, T, U, V) -> Unit
    ) {
        a.get(id).ifPresent { (_, aD) ->
            b.get(id).ifPresent { (_, bD) ->
                c.get(id).ifPresent { (i, cD) ->
                    consumer.invoke(i, aD, bD, cD)
                }
            }
        }
    }

}