package br.com.kassin.saochypets.data.repository;

import br.com.kassin.saochypets.manager.LevelingManager;
import br.com.kassin.saochypets.pet.PetAccess;
import br.com.kassin.saochypets.pet.PetBehavior;
import br.com.kassin.saochypets.data.cache.PetCache;
import br.com.kassin.saochypets.data.model.Pet;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLPlayerPetRepository implements PlayerPetRepository {

    private final HikariDataSource dataSource;

    public MySQLPlayerPetRepository(HikariDataSource source) {
        this.dataSource = source;
        createTableIfNotExists();
    }

    @Override
    public Optional<Map<String, Pet>> getPets(UUID playerId) {
        Map<String, Pet> playerPets = new ConcurrentHashMap<>();
        String sql = "SELECT pet_id, display_name, level, xp, access, rarity, behavior FROM player_pets WHERE player_uuid = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String petId = rs.getString("pet_id");
                    Pet template = PetCache.getPet(petId);
                    if (template == null) continue;

                    Pet pet = buildPet(template, rs);
                    playerPets.put(petId, pet);
                }
            }
            return Optional.of(playerPets);

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Pet> getPet(UUID playerId, String petId) {
        String sql = "SELECT display_name, level, xp, access, rarity FROM player_pets WHERE player_uuid = ? AND pet_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, petId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Pet template = PetCache.getPet(petId);
                if (template == null) return Optional.empty();

                Pet pet = buildPet(template, rs);
                return Optional.of(pet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void addPet(UUID playerId, Pet pet) {
        String sql = "INSERT IGNORE INTO player_pets " +
                "(player_uuid, pet_id, level, xp, access, rarity, display_name, behavior) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, pet.getPetId());
            ps.setInt(3, Math.max(1, pet.getLevel()));
            ps.setInt(4, Math.max(0, pet.getXp()));
            ps.setString(5, pet.getAccess().name());
            ps.setString(6, pet.getRarity().id());
            ps.setString(7, pet.getDisplayName());
            ps.setString(8, pet.getBehavior().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePet(UUID playerId, Pet pet) {
        String sql = "UPDATE player_pets SET level = ?, xp = ?, access = ?, rarity = ?, display_name = ?, behavior = ? " +
                "WHERE player_uuid = ? AND pet_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Math.max(1, pet.getLevel()));
            ps.setInt(2, Math.max(0, pet.getXp()));
            ps.setString(3, pet.getAccess().name());
            ps.setString(4, pet.getRarity().id());
            ps.setString(5, pet.getDisplayName());
            ps.setString(6, pet.getBehavior().name());
            ps.setString(7, playerId.toString());
            ps.setString(8, pet.getPetId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeOwnedPet(UUID playerId, String petId) {
        String sql = "DELETE FROM player_pets WHERE player_uuid = ? AND pet_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, petId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS player_pets (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "pet_id VARCHAR(255) NOT NULL," +
                "display_name VARCHAR(255) NOT NULL," +
                "level INT NOT NULL DEFAULT 1," +
                "xp INT NOT NULL DEFAULT 0," +
                "access VARCHAR(16) NOT NULL DEFAULT 'PRIVATE'," +
                "rarity VARCHAR(32) NOT NULL DEFAULT 'COMMON'," +
                "behavior VARCHAR(16) NOT NULL DEFAULT 'PASSIVE'," +
                "PRIMARY KEY (player_uuid, pet_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute(ddl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Pet buildPet(Pet template, ResultSet rs) throws SQLException {
        return Pet.builder()
                .petId(template.getPetId())
                .displayName(rs.getString("display_name") == null ? template.getDisplayName() : rs.getString("display_name"))
                .baseEntity(template.getBaseEntity())
                .modelID(template.getModelID())
                .mountable(template.isMountable())
                .baseDamage(template.getBaseDamage())
                .canFly(template.canFly())
                .delayWhenAttacking(template.getDelayWhenAttacking())
                .attackAnimation(template.getAttackAnimation())
                .speed(template.getSpeed())
                .aggressive(template.isAggressive())
                .level(rs.getInt("level"))
                .xp(rs.getInt("xp"))
                .rarity(LevelingManager.getRarityById(rs.getString("rarity")))
                .attackRange(template.getAttackRange())
                .behavior(PetBehavior.valueOf(rs.getString("behavior")))
                .access(PetAccess.valueOf(rs.getString("access")))
                .build();
    }

}