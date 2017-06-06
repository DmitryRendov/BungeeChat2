package dev.aura.bungeechat.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import dev.aura.bungeechat.BungeeChat;
import dev.aura.bungeechat.api.account.BungeeChatAccount;
import dev.aura.bungeechat.api.account.BungeeChatAccountStorage;
import dev.aura.bungeechat.api.enums.ChannelType;
import dev.aura.bungeechat.util.LoggerHelper;
import lombok.Cleanup;

public class AccountFileStorage implements BungeeChatAccountStorage {
    private static final String FILE_EXTENSION = ".sav";

    private static File getUserDataDir() {
        File folder = new File(BungeeChat.getInstance().getConfigFolder(), "userdata");
        folder.mkdirs();

        return folder;
    }

    @Override
    public void save(BungeeChatAccount account) {
        try {
            File accountFile = new File(getUserDataDir(), account.getUniqueId() + FILE_EXTENSION);

            if (!accountFile.exists()) {
                accountFile.createNewFile();
            }

            @Cleanup
            FileOutputStream saveFile = new FileOutputStream(accountFile);
            @Cleanup
            ObjectOutputStream save = new ObjectOutputStream(saveFile);

            save.writeObject(account.getChannelType());
            save.writeObject(account.hasMessangerEnabled());
            save.writeObject(account.isVanished());
            save.writeObject(account.hasSocialSpyEnabled());
            save.writeObject(account.getIgnored());
            save.writeObject(account.getStoredPrefix().orElse(null));
            save.writeObject(account.getStoredSuffix().orElse(null));
        } catch (IOException e) {
            LoggerHelper.warning("Could not save player " + account, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BungeeChatAccount load(UUID uuid) {
        try {
            File accountFile = new File(getUserDataDir(), uuid.toString() + FILE_EXTENSION);

            if (!accountFile.exists())
                return new Account(uuid);

            @Cleanup
            FileInputStream saveFile = new FileInputStream(accountFile);
            @Cleanup
            ObjectInputStream save = new ObjectInputStream(saveFile);

            return new Account(uuid, (ChannelType) save.readObject(), (boolean) save.readObject(),
                    (boolean) save.readObject(), (boolean) save.readObject(), (BlockingQueue<UUID>) save.readObject(),
                    Optional.ofNullable((String) save.readObject()), Optional.ofNullable((String) save.readObject()));
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            BungeeChatAccount account = new Account(uuid);

            LoggerHelper.warning("Could not load player " + account, e);

            return account;
        }
    }
}