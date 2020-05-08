package gen3check;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import gen3check.gui.PokemonFoundPanel;
import gen3check.observers.PokemonListContainerObserver;
import gen3check.pokemon.data.StatPack;
import rng.*;

public class ToolEngine {

    private PokemonListContainer elc;

    public ToolEngine() {
        this.elc = new PokemonListContainer();
    }

    /**
     * Calls eventSelectionChange
     */
    public void onPokemonGridSelectionChanged(int index) {
        this.elc.onPokemonGridSelectionChanged(index);
    }

    /****************************************/

    public void quit() {
        System.exit(0);
    }

    public void addPokemonListContainerObserver(PokemonListContainerObserver observer) {
        this.elc.addObserver(observer);
    }

    public int getTrainerID() {
        return this.elc.getTrainerID();
    }

    public int getPokemonID() {
        return this.elc.getPokemonID();
    }

    public PokemonFoundData getPokemon(int index) {
        return this.elc.getPokemon(index);
    }

    /////////////////////////////////////////

    public void search(int minFrame, int maxFrame, int trainerID, int pokemonID, StatPack spminus, StatPack spneutral,
        StatPack spplus, JCheckBox[] natures) {
        this.elc.clear();
        this.elc.setPokemonID(pokemonID);
        this.elc.setTrainerID(trainerID);
        Seed s = new Seed(trainerID);
        List<PokemonFoundData> tempEventList = new ArrayList<>();
        PokemonFoundData lastPokemonFoundData = null;
        int curClusterSize = 0;
        int curClusterScore = 0;
        int bestClusterScore = 1;
        int curClusterFrame = minFrame;
        int bestClusterFrame = minFrame;
        int bestClusterEndFrame = maxFrame;
        int curClusterListIndex = 0;
        int bestClusterListIndex = 0;

        boolean addClusters = true;

        for (int i = minFrame; i <= maxFrame; i++) {
            PokemonRNG pkmRNG = new PokemonMethod1(s, i);
            if (natures[pkmRNG.nature.getID()].isSelected() && !pkmRNG.isShiny()) {
                StatPack sp = new StatPack(spneutral.hp, spneutral.atk, spneutral.def, spneutral.spa, spneutral.spd,
                    spneutral.spe);
                for (int k = 1; k < 6; k++) {
                    if (pkmRNG.nature.getNatureBoost(k) > 1.01)
                        sp.setStat(k, spplus.getStat(k));
                    if (pkmRNG.nature.getNatureBoost(k) < 0.99)
                        sp.setStat(k, spminus.getStat(k));
                }
                if (sp.hp <= pkmRNG.hp && sp.atk <= pkmRNG.atk && sp.def <= pkmRNG.def && sp.spa <= pkmRNG.spa
                    && sp.spd <= pkmRNG.spd && sp.spe <= pkmRNG.spe) {
                    if (addClusters) {
                        if (lastPokemonFoundData == null) {
                            lastPokemonFoundData = new PokemonFoundData(pkmRNG, i);
                            curClusterFrame = i;
                            curClusterListIndex = 0;
                            curClusterSize++;
                        } else {
                            int diff = i - lastPokemonFoundData.getFrame();
                            if (diff <= 2) {
                                tempEventList.add(lastPokemonFoundData);
                                lastPokemonFoundData = new PokemonFoundData(pkmRNG, i);
                                if (diff == 2) {
                                    curClusterScore++;
                                } else if (diff == 1) {
                                    curClusterScore += 2;
                                }
                                curClusterSize++;
                            } else if (curClusterSize == 1) {
                                lastPokemonFoundData = new PokemonFoundData(pkmRNG, i);
                                curClusterFrame = i;
                                curClusterListIndex = tempEventList.size();
                                curClusterScore = 0;
                                // curClusterSize = 1;
                            } else if (curClusterSize > 1) {
                                tempEventList.add(lastPokemonFoundData);
                                lastPokemonFoundData = new PokemonFoundData(pkmRNG, i);
                                curClusterSize = 1;
                                if (curClusterScore > bestClusterScore) {
                                    bestClusterFrame = curClusterFrame;
                                    bestClusterScore = curClusterScore;
                                    bestClusterEndFrame = i;
                                    bestClusterListIndex = curClusterListIndex;
                                }
                                curClusterFrame = i;
                                curClusterScore = 0;
                                curClusterListIndex = tempEventList.size() - 1;
                            } else {
                                System.out.println("Missed case!");
                            }
                        }
                    } else {
                        this.elc.addPokemon(new PokemonFoundData(pkmRNG, i));
                    }
                }
                this.elc.update();
            }
        }
        
        // lazy, fix later
        for (int i = bestClusterListIndex; i < tempEventList.size(); i++) {
            PokemonFoundData pokemonFoundData = tempEventList.get(i);
            if (i == bestClusterListIndex && pokemonFoundData.getFrame() != bestClusterFrame) {
                System.out.printf("bestClusterListIndex doesn't match bestClusterFrame! bestClusterFrame: %d, getFrame(): %d\n",
                        bestClusterFrame - PokemonFoundPanel.FIXED_RNG_ADVANCES, pokemonFoundData.getFrame() - PokemonFoundPanel.FIXED_RNG_ADVANCES);
            } if (pokemonFoundData.getFrame() == bestClusterEndFrame) {
                break;
            }
            this.elc.addPokemon(pokemonFoundData);
        }

        this.elc.update();

        System.out.printf("bestClusterScore: %d, bestClusterFrame: %d\n", bestClusterScore, bestClusterFrame - PokemonFoundPanel.FIXED_RNG_ADVANCES);
        
        
    }

}
