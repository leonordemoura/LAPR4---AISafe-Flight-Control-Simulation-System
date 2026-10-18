package aiSafe.infrastructure.bootstrap;

import aiSafe.aircraftmanagement.domain.Country;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.MakerName;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;


public class MakerBootstrapper {
    private final MakerRepository repository = PersistenceContext.repositories().getMakerRepository();

    public void execute(){
        registerIfMissing(123L, "Boeing", "USA", 840);
        registerIfMissing(124L, "Airbus", "France", 250);
    }

    private void registerIfMissing(
            final Long makerId,
            final String name,
            final String countryName,
            final Integer countryNumericCode
    ) {
        final MakerID makerID = MakerID.valueOf(makerId);
        if (repository.findByID(makerID).isPresent()) {
            System.out.println("Maker with ID " + makerId.intValue() + " already exists. Skipping.");
            return;
        }

        MakerName makerName = MakerName.valueOf(name);
        if (repository.findByName(makerName).isPresent()){
            System.out.println("A maker with that name already exists, use another name!");
            return;
        }

        final Maker maker = new Maker(makerID, MakerName.valueOf(name), Country.valueOf(countryName, countryNumericCode));
        try{
            repository.save(maker);
            System.out.println("Maker created: " + maker.name().name() + " - " + makerId.intValue());
        } catch(ConcurrencyException | IntegrityViolationException ex){
            System.out.println("Maker with id " + makerID.id() + " already exists. Skipping.");
        }
    }
}
