package br.com.kassin.saochypets.data;

import br.com.kassin.saochypets.data.model.Pet;
import com.ticxo.modelengine.api.model.ModeledEntity;

public record ActivePet(Pet petData, ModeledEntity modeledEntity) {

    public void destroy() {
        if (modeledEntity != null) {
            modeledEntity.destroy();
        }
    }
}