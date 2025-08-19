package br.com.kassin.saochypets;

import br.com.kassin.saochypets.commands.PetCommand;
import br.com.kassin.saochypets.data.DatabaseSource;
import br.com.kassin.saochypets.data.PetService;
import br.com.kassin.saochypets.data.PlayerPetDataProvider;
import br.com.kassin.saochypets.data.cache.PetCache;
import br.com.kassin.saochypets.data.cache.PlayerActivePetCache;
import br.com.kassin.saochypets.data.repository.MySQLPlayerPetRepository;
import br.com.kassin.saochypets.data.repository.PlayerPetRepository;
import br.com.kassin.saochypets.listeners.PetGUIListener;
import br.com.kassin.saochypets.listeners.PetListener;
import br.com.kassin.saochypets.listeners.PetXpListener;
import br.com.kassin.saochypets.listeners.RenamePetChat;
import br.com.kassin.saochypets.loader.PetConfigLoader;
import br.com.kassin.saochypets.manager.LevelingManager;

public class SaochyPetsInitializer {
    private final SaochyPetsPlugin plugin;
    private final PetService petService;
    private final PlayerPetDataProvider dataProvider;
    private final PetConfigLoader petConfigLoader;

    private SaochyPetsInitializer(SaochyPetsPlugin plugin) {
        this.plugin = plugin;
        final DatabaseSource databaseSource = DatabaseSource.create(plugin);
        final PlayerPetRepository repository = new MySQLPlayerPetRepository(databaseSource.getSource());
        petConfigLoader = new PetConfigLoader(plugin);
        dataProvider = new PlayerPetDataProvider(repository);
        petService = new PetService(dataProvider);
    }

    public static SaochyPetsInitializer of(SaochyPetsPlugin plugin) {
        return new SaochyPetsInitializer(plugin);
    }

    public void enable() {
        LevelingManager.load(plugin.getLevelsAndRaritiesConfig(), plugin.getLogger());
        petConfigLoader.loadPets();
        plugin.getCommand("pet").setExecutor(new PetCommand(petService, dataProvider));
        plugin.getServer().getPluginManager().registerEvents(new PetListener(petService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PetGUIListener(petService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RenamePetChat(petService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PetXpListener(petService), plugin);
    }

    public void reloadConfig() {
        PlayerActivePetCache.clear();
        PetCache.clear();
        LevelingManager.load(plugin.getLevelsAndRaritiesConfig(), plugin.getLogger());
        petConfigLoader.loadPets();
        dataProvider.clearCache();
    }

}
