/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fmllegacy.network;

import net.minecraftforge.fmllegacy.network.event.EventNetworkChannel;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegistryManager;

import java.util.Arrays;
import java.util.List;

class NetworkInitialization {

    public static SimpleChannel getHandshakeChannel() {
        SimpleChannel handshakeChannel = NetworkRegistry.ChannelBuilder.
                named(FMLNetworkConstants.FML_HANDSHAKE_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION).
                simpleChannel();

        handshakeChannel.messageBuilder(FMLHandshakeMessages.C2SAcknowledge.class, 99, NetworkDirection.LOGIN_TO_SERVER).
                loginIndex(FMLHandshakeMessages.LoginIndexedMessage::getLoginIndex, FMLHandshakeMessages.LoginIndexedMessage::setLoginIndex).
                decoder(FMLHandshakeMessages.C2SAcknowledge::decode).
                encoder(FMLHandshakeMessages.C2SAcknowledge::encode).
                consumer(FMLHandshakeHandler.indexFirst(FMLHandshakeHandler::handleClientAck)).
                add();

        handshakeChannel.messageBuilder(FMLHandshakeMessages.S2CModList.class, 1, NetworkDirection.LOGIN_TO_CLIENT).
                loginIndex(FMLHandshakeMessages.LoginIndexedMessage::getLoginIndex, FMLHandshakeMessages.LoginIndexedMessage::setLoginIndex).
                decoder(FMLHandshakeMessages.S2CModList::decode).
                encoder(FMLHandshakeMessages.S2CModList::encode).
                markAsLoginPacket().
                consumer(FMLHandshakeHandler.biConsumerFor(FMLHandshakeHandler::handleServerModListOnClient)).
                add();

        handshakeChannel.messageBuilder(FMLHandshakeMessages.C2SModListReply.class, 2, NetworkDirection.LOGIN_TO_SERVER).
                loginIndex(FMLHandshakeMessages.LoginIndexedMessage::getLoginIndex, FMLHandshakeMessages.LoginIndexedMessage::setLoginIndex).
                decoder(FMLHandshakeMessages.C2SModListReply::decode).
                encoder(FMLHandshakeMessages.C2SModListReply::encode).
                consumer(FMLHandshakeHandler.indexFirst(FMLHandshakeHandler::handleClientModListOnServer)).
                add();

        handshakeChannel.messageBuilder(FMLHandshakeMessages.S2CRegistry.class, 3, NetworkDirection.LOGIN_TO_CLIENT).
                loginIndex(FMLHandshakeMessages.LoginIndexedMessage::getLoginIndex, FMLHandshakeMessages.LoginIndexedMessage::setLoginIndex).
                decoder(FMLHandshakeMessages.S2CRegistry::decode).
                encoder(FMLHandshakeMessages.S2CRegistry::encode).
                buildLoginPacketList(RegistryManager::generateRegistryPackets). //TODO: Make this non-static, and store a cache on the client.
                consumer(FMLHandshakeHandler.biConsumerFor(FMLHandshakeHandler::handleRegistryMessage)).
                add();

        handshakeChannel.messageBuilder(FMLHandshakeMessages.S2CConfigData.class, 4, NetworkDirection.LOGIN_TO_CLIENT).
                loginIndex(FMLHandshakeMessages.LoginIndexedMessage::getLoginIndex, FMLHandshakeMessages.LoginIndexedMessage::setLoginIndex).
                decoder(FMLHandshakeMessages.S2CConfigData::decode).
                encoder(FMLHandshakeMessages.S2CConfigData::encode).
                buildLoginPacketList(ConfigSync.INSTANCE::syncConfigs).
                consumer(FMLHandshakeHandler.biConsumerFor(FMLHandshakeHandler::handleConfigSync)).
                add();

        return handshakeChannel;
    }

    public static SimpleChannel getPlayChannel() {
         SimpleChannel playChannel = NetworkRegistry.ChannelBuilder.
                named(FMLNetworkConstants.FML_PLAY_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION).
                simpleChannel();

        playChannel.messageBuilder(FMLPlayMessages.SpawnEntity.class, 0).
                decoder(FMLPlayMessages.SpawnEntity::decode).
                encoder(FMLPlayMessages.SpawnEntity::encode).
                consumer(FMLPlayMessages.SpawnEntity::handle).
                add();

        playChannel.messageBuilder(FMLPlayMessages.OpenContainer.class,1).
                decoder(FMLPlayMessages.OpenContainer::decode).
                encoder(FMLPlayMessages.OpenContainer::encode).
                consumer(FMLPlayMessages.OpenContainer::handle).
                add();

        return playChannel;
    }

    public static List<EventNetworkChannel> buildMCRegistrationChannels() {
        final EventNetworkChannel mcRegChannel = NetworkRegistry.ChannelBuilder.
                named(FMLNetworkConstants.MC_REGISTER_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION).
                eventNetworkChannel();
        mcRegChannel.addListener(FMLMCRegisterPacketHandler.INSTANCE::registerListener);
        final EventNetworkChannel mcUnregChannel = NetworkRegistry.ChannelBuilder.
                named(FMLNetworkConstants.MC_UNREGISTER_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION).
                eventNetworkChannel();
        mcUnregChannel.addListener(FMLMCRegisterPacketHandler.INSTANCE::unregisterListener);
        return Arrays.asList(mcRegChannel, mcUnregChannel);
    }
}
