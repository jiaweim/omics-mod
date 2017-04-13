package omics.mod.controller.impl;

import omics.mod.io.unimod.xml.UnimodReader;
import omics.mod.model.PTM;
import omics.mod.model.Specificity;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import omics.mod.controller.AbstractDataAccessController;
import omics.mod.exception.DataAccessException;
import omics.mod.io.unimod.model.AtomComposition;
import omics.mod.io.unimod.model.NeutralLoss;
import omics.mod.io.unimod.model.Unimod;
import omics.mod.io.unimod.model.UnimodModification;
import omics.mod.model.UniModPTM;

import javax.xml.bind.JAXBException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Class to retrieve all Modifications from Unimod database. All modifications are store in
 * memory using the
 * yperez.
 */
public class UnimodDataAccessController extends AbstractDataAccessController {

    private static final Logger logger = LoggerFactory.getLogger(UnimodDataAccessController.class);

    public UnimodDataAccessController(InputStream xml) {
        super(xml);
        try {
            UnimodReader reader = new UnimodReader(xml);
            initPTMMap(reader.getUnimodObject());

        } catch (JAXBException e) {
            String msg = "Exception while trying to read the Unimod file";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }


    public static void main(String[] args) {
        InputStream unimodUrl = UnimodDataAccessController.class.getClassLoader().getResourceAsStream("unimod.xml");
        UnimodDataAccessController controller = new UnimodDataAccessController(unimodUrl);
    }

    /**
     * Init the Map of the PTMs
     *
     * @param unimodObject
     */
    private void initPTMMap(Unimod unimodObject) {

        ptmMap = new HashMap<>(unimodObject.getModifications().getMod().size());

        Document doc = new Document();
        Element rootElement = new Element("mods");
        doc.setRootElement(rootElement);

        Integer id = 1;

        for (UnimodModification unimodMod : unimodObject.getModifications().getMod()) {
            Element child = new Element("mod");
            child.setAttribute("id", unimodMod.getRecordId().toString());
            child.setAttribute("title", unimodMod.getTitle());
            child.setAttribute("description", unimodMod.getFullName());

            List<AtomComposition> atomCompositions = unimodMod.getDelta().getElement();
            child.setAttribute("composition", convertToString(atomCompositions));


            /*
             * We will add the UNIMOD to the id in order to have the same style than PSI-MOD and
             * the mzIdentML files
             */
            String accession = "UNIMOD:" + (unimodMod.getRecordId()).intValue();
            String name = unimodMod.getTitle();
            String description = unimodMod.getFullName();
            String formula = (unimodMod.getDelta() != null) ? unimodMod.getDelta().getComposition() : null;
            Double avgMass = (unimodMod.getDelta() != null) ? (unimodMod.getDelta().getAvgeMass()).doubleValue() : null;
            Double monoMass = (unimodMod.getDelta() != null) ? (unimodMod.getDelta().getMonoMass()).doubleValue() :
                    null;

            List<Element> siteEles = new ArrayList<>();
            List<Element> neuEles = new ArrayList<>();
            List<Specificity> specificityList = null;

            if (unimodMod.getSpecificity() != null && unimodMod.getSpecificity().size() > 0) {
                specificityList = new ArrayList<>(unimodMod.getSpecificity().size());
                for (omics.mod.io.unimod.model.Specificity oldSpecificty : unimodMod
                        .getSpecificity()) {
                    Specificity specificity = new Specificity(oldSpecificty.getSite(), oldSpecificty.getPosition());
                    specificityList.add(specificity);

                    Element specElement = new Element("specificity");
                    specElement.setAttribute("site", oldSpecificty.getSite());
                    specElement.setAttribute("position", oldSpecificty.getPosition());

                    List<NeutralLoss> neutralLossList = oldSpecificty.getNeutralLoss();
                    if (neutralLossList != null && !neutralLossList.isEmpty()) {

                        Element neutralLosses = new Element("neutralloss_list");

                        for (NeutralLoss neutralLoss : neutralLossList) {
                            List<AtomComposition> element = neutralLoss.getElement();
                            String comp = convertToString(element);
                            if(comp.isEmpty())
                                continue;

                            Element neutralLossEle = new Element("neutralloss");
                            neutralLossEle.setAttribute("name", neutralLoss.getComposition());
                            neutralLossEle.setAttribute("shortname", neutralLoss.getComposition());

                            neutralLossEle.setAttribute("composition", comp);

                            neutralLosses.addContent(neutralLossEle);
                        }

                        specElement.addContent(neutralLosses);
                    }

                    child.addContent(specElement);
                }

            }
            PTM uniModPTM = new UniModPTM(accession, name, description, monoMass, avgMass, specificityList, formula);
            ptmMap.put(accession, uniModPTM);

            rootElement.addContent(child);
            id++;
        }

        rootElement.setAttribute("count", String.valueOf(id));

        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        try {
            xmlOutputter.output(doc, new FileWriter("modifications.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String convertToString(List<AtomComposition> atomCompositions) {
        StringBuffer sb = new StringBuffer();
        for (AtomComposition composition : atomCompositions) {
            String symbol = composition.getSymbol();
            int index = 0;
            for (char character : symbol.toCharArray()) {
                if (Character.isDigit(character)) {
                    index++;
                } else {
                    break;
                }
            }
            if (index > 0) {
                int isotope = Integer.parseInt(symbol.substring(0, index));
                String element = symbol.substring(index);
                sb.append(element).append("[").append(isotope).append("]");
            } else {
                sb.append(symbol);
            }
            int number = composition.getNumber().intValue();
            if(number > 1){
                sb.append(number);
            }
        }
        return sb.toString();
    }

    /**
     * We decided to put the UNIOMD: prefix in order to be compatible with the same PSI-MOD style and
     * the mzIdentML.
     *
     * @param accession an accession in a way of UNIMOD:(number) can be also a number in the case of a number we added
     *                  the prefix UNIMOD at the beginning.
     * @return
     */
    @Override
    public PTM getPTMbyAccession(String accession) {
        if (!accession.contains("UNIMOD:"))
            accession = "UNIMOD:" + accession;
        return super.getPTMbyAccession(accession);
    }
}
