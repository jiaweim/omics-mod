package omics.mod.jaxb.xml;

import omics.mod.io.unimod.model.*;
import omics.mod.io.unimod.xml.UnimodReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: yperez
 * Date: 19/07/11
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class UnimodReaderTest
{
    @Test
    public void unimodReader() throws JAXBException, FileNotFoundException
    {
        InputStream inputStream = new FileInputStream("D:\\database\\modification\\unimod.xml");
        int count = 0;

        Document doc = new Document();
        Element root = new Element("mods");
        doc.setRootElement(root);

        UnimodReader unimodreader = new UnimodReader(inputStream);
        List<UnimodModification> modifications = unimodreader.getUnimodObject().getModifications().getMod();
        for (UnimodModification modification : modifications) {
            count++;

            String id = modification.getRecordId().toString();

            String title = modification.getTitle();
            String description = modification.getFullName();
            String miscNotes = modification.getMiscNotes(); // maybe null

            Element modElement = new Element("mod");
            modElement.setAttribute("id", String.valueOf("UNIMOD:" + id));
            modElement.setAttribute("title", title);
            modElement.setAttribute("desp", description);
            modElement.setAttribute("user", "admin");

            Delta delta = modification.getDelta();
            List<AtomComposition> element = delta.getElement();
            String formula = toFormula(element);

            Element deltaElement = new Element("delta");
            deltaElement.setAttribute("formula", formula);
            deltaElement.setAttribute("mono_mass", delta.getMonoMass().toString());
//            deltaElement.setAttribute("avge_mass", delta.getAvgeMass().toString());
            modElement.addContent(deltaElement);

            HashMap<NL, Set<String>> nlMap = new HashMap<>();
            List<Site> sites = new ArrayList<>();
            Set<String> aaSet = new HashSet<>();
            List<Specificity> specificity = modification.getSpecificity();
            for (Specificity asp : specificity) {

                Element siteElement = new Element("site");
                siteElement.setAttribute("aa", asp.getSite());
                siteElement.setAttribute("pos", asp.getPosition());
                siteElement.setAttribute("class", asp.getClassification());
//
//                String position = asp.getPosition();
//                String aa = asp.getSite();
//
//                aaSet.add(aa);
//
//                Site site = new Site(position, aa);
//                sites.add(site);

                List<NeutralLoss> lossList = asp.getNeutralLoss();
                for (NeutralLoss loss : lossList) {
                    List<AtomComposition> lossElement = loss.getElement();
                    String lossFormula = toFormula(lossElement);
                    if (lossFormula.isEmpty())
                        continue;
                    double lossMass = loss.getMonoMass().doubleValue();
//                    double avgMass = loss.getAvgeMass().doubleValue();

                    Element nlElement = new Element("NeutralLoss");
                    nlElement.setAttribute("formula", lossFormula);
                    nlElement.setAttribute("mono_mass", loss.getMonoMass().toString());

                    siteElement.addContent(nlElement);

//                    NL nl = new NL(lossFormula, lossMass, avgMass);
//                    if (nlMap.containsKey(nl))
//                        nlMap.get(nl).add(aa);
//                    else {
//                        HashSet set = new HashSet();
//                        set.add(aa);
//                        nlMap.put(nl, set);
//                    }
                }
                modElement.addContent(siteElement);
            }

//            if (aaSet.size() != sites.size()) {
//                System.out.println(count);
//            }
//            for (Site site : sites) {
//                Element siteElement = new Element("site");
//                siteElement.setAttribute("aa", site.aa);
//                siteElement.setAttribute("pos", site.pos);
//                modElement.addContent(siteElement);
//            }
//
//
//            if (nlMap.size() > 0) {
//                for (NL nl : nlMap.keySet()) {
//                    if (nl.formua.isEmpty())
//                        continue;
//                    Element nlElement = new Element("NeutralLoss");
//                    nlElement.setAttribute("formula", nl.formua);
//                    nlElement.setAttribute("mono_mass", String.valueOf(nl.mono_mass));
//                    Set<String> strings = nlMap.get(nl);
//                    StringJoiner joiner = new StringJoiner(":");
//                    for (String aa : strings) {
//                        joiner.add(aa);
//                    }
//                    nlElement.setAttribute("sites", joiner.toString());
//                    modElement.addContent(nlElement);
//                }
//            }

            if (miscNotes != null && !miscNotes.isEmpty()) {
                Element note = new Element("misc_note");
                note.setText(miscNotes);
                modElement.addContent(note);
            }

            root.addContent(modElement);
        }

        root.setAttribute("count", String.valueOf(count));

        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        try {
            xmlOutputter.output(doc, new FileWriter("D:\\database\\modification\\mods.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String toFormula(List<AtomComposition> atomCompositions)
    {
        List<String> list = new ArrayList<>();
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
            StringBuilder atomBuilder = new StringBuilder();
            if (index > 0) {
                int isotope = Integer.parseInt(symbol.substring(0, index));
                String element = symbol.substring(index);
                atomBuilder.append(element).append("[").append(isotope).append(']');
            } else {
                atomBuilder.append(symbol);
            }
            int number = composition.getNumber().intValue();
            if (number == 0)
                continue;
            if (number != 1)
                atomBuilder.append(number);
            list.add(atomBuilder.toString());
        }

        list.sort(Comparator.naturalOrder());
        StringJoiner joiner = new StringJoiner("");
        for (String atom : list)
            joiner.add(atom);
        return joiner.toString();
    }

    private static class Site
    {
        private String pos;
        private String aa;
        private String note;

        public Site(String pos, String aa)
        {
            this.pos = pos;
            this.aa = aa;
        }

        public String getPos()
        {
            return pos;
        }

        public String getAa()
        {
            return aa;
        }

        public String getNote()
        {
            return note;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Site site = (Site) o;

            if (!pos.equals(site.pos)) return false;
            return aa.equals(site.aa);
        }

        @Override
        public int hashCode()
        {
            int result = pos.hashCode();
            result = 31 * result + aa.hashCode();
            return result;
        }
    }

    private static class NL
    {
        private String formua;
        private double mono_mass;
        private double avge_mass;
        private Set<String> aaSet;

        NL(String formua, double mono_mass, double avge_mass)
        {
            this.formua = formua;
            this.mono_mass = mono_mass;
            this.avge_mass = avge_mass;
            this.aaSet = new HashSet<>();
        }

        public void add(String aa)
        {
            this.aaSet.add(aa);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NL nl = (NL) o;

            return formua.equals(nl.formua);
        }

        @Override
        public int hashCode()
        {
            return formua.hashCode();
        }
    }

}
