package omics.mod.io.pridemod.extractor;

import omics.mod.io.pridemod.model.PrideMod;
import omics.mod.io.pridemod.model.PrideModification;
import omics.mod.io.pridemod.model.PrideModifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: yperez
 * Date: 19/07/11
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
public class PrideModExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PrideModExtractor.class);

    private PrideMod prideMod = null;

    private PrideModifications modColletion = null;

    public PrideModExtractor(PrideMod prideMod) {
        this.prideMod = prideMod;
    }

    public PrideModExtractor(Unmarshaller unmarshaller) {
        this.prideMod = (PrideMod) unmarshaller;
    }

    public List<PrideModification> getModListbyMass(double mass) {
        return this.modColletion.getModbyMonoMass(mass);
    }

    public PrideModification getModbyId(int id) {
        return this.modColletion.getModbyId(id);
    }

    public List<PrideModification> getModListbySpecificity(String specificity) {
        return this.modColletion.getModListbySpecificity(specificity);
    }

    public List<PrideModification> getModListbyMassSepecificity(String specificity, double mass) {
        return this.modColletion.getListbyMassSpecificity(specificity, mass);
    }


}
