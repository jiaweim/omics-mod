package omics.mod.controller.impl;

import omics.mod.controller.AbstractDataAccessController;
import omics.mod.io.pridemod.model.PrideModification;
import omics.mod.io.pridemod.xml.PrideModReader;
import omics.mod.model.PTM;
import omics.mod.model.Specificity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import omics.mod.io.pridemod.model.PrideMod;
import omics.mod.model.PRIDEModPTM;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * yperez
 */
public class PRIDEModDataAccessController extends AbstractDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(PRIDEModDataAccessController.class);

    /**
     * Default constructor for Controllers
     *
     * @param xml
     */
    public PRIDEModDataAccessController(InputStream xml) {
        super(xml);

        PrideModReader reader = new PrideModReader(xml);
        initPTMMap(reader.getPrideMod());
    }

    /**
     * Init the PTM map.
     *
     * @param prideMod
     */
    private void initPTMMap(PrideMod prideMod) {
        if (prideMod != null) {
            ptmMap = new HashMap<Comparable, PTM>();
            for (PrideModification oldMod : prideMod.getPrideModifications().getPrideModification()) {
                String accession = oldMod.getPsiId();
                String name = oldMod.getPsiName();
                Double monoMass = oldMod.getDiffMono().doubleValue();
                List<Specificity> specicityList = oldMod.getSpecificityList();
                Comparable unimodReference = String.valueOf(oldMod.getUnimodMappings().getUnimodMapping().get(0)
                        .getId().intValue());
                PRIDEModPTM ptm = new PRIDEModPTM(accession, name, name, monoMass, null, specicityList,
                        unimodReference, null);
                ptmMap.put(accession, ptm);
            }
        }
    }

}
