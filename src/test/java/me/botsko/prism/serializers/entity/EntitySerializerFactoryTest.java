package me.botsko.prism.serializers.entity;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 18/11/2020.
 */
class EntitySerializerFactoryTest {
    /**
     * Required to avoid NPE.
     */
    private ServerMock server;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
    }

    @Test
    public void constructorTest(){
        EntitySerializerFactory factory = EntitySerializerFactory.get();
        assertEquals(EntitySerializerFactory.getSerializer(EntityType.HORSE),AbstractHorseSerializer.class);
    }

}