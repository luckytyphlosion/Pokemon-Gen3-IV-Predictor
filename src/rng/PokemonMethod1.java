package rng;

import rng.PokemonRNG;
import gen3check.gui.PokemonFoundPanel;
import gen3check.pokemon.data.Nature;

public class PokemonMethod1 extends PokemonRNG {

    public PokemonMethod1(int pid, RNG rng) {
        super(pid, rng);
    }

    public PokemonMethod1(Seed seed, int frame) {
        this.frame = frame + PokemonFoundPanel.FIXED_RNG_ADVANCES;
        RNG rng1 = new RNG(seed);
        RNG rng2 = new RNG(seed);
        rng1.advance(this.frame);
        rng2.copy(rng1);
        rng1.advance();
        this.pid = ((long) rng1.getTop() << 16) + (long) rng2.getTop();
        this.nature = new Nature((int) (this.pid % 25));
        this.generate(rng1);

        rng1.advance(); // lag frame

        rng1.advance();
        if (rng1.getTop() <= 0x3f) {
            this.setShiny();
        }

        rng1.advance(); // lag frame

        rng1.advance();
        int firstIV = rng1.getTop() % 6;
        this.setIV(firstIV, 31);
        int secondIV;
        do {
            rng1.advance();
            secondIV = rng1.getTop() % 6;
        } while (firstIV == secondIV);
        this.setIV(secondIV, 31);
    }

    public PokemonMethod1(int rawSeed) {
        RNG rng1 = new RNG(rawSeed);
        this.pid = ((long)rng1.getAndAdvance() | (long)rng1.getAndAdvance() << 16);
        this.nature = new Nature((int) (this.pid % 25));
        this.generate(rng1);
        
        rng1.advance(); // lag frame

        rng1.advance();
        if (rng1.getTop() <= 0x3f) {
            this.setShiny();
        }

        rng1.advance(); // lag frame

        rng1.advance();
        int firstIV = rng1.getTop() % 6;
        this.setIV(firstIV, 31);
        int secondIV;
        do {
            rng1.advance();
            secondIV = rng1.getTop() % 6;
        } while (firstIV == secondIV);
        this.setIV(secondIV, 31);
    }

    private void setIV(int ivNum, int ivValue) {
        switch (ivNum) {
        case 0:
            this.hp = ivValue;
            break;
        case 1:
            this.atk = ivValue;
            break;
        case 2:
            this.def = ivValue;
            break;
        case 3:
            this.spe = ivValue;
            break;
        case 4:
            this.spa = ivValue;
            break;
        case 5:
            this.spd = ivValue;
            break;
        default:
            throw new RuntimeException("Illegal ivNum! (ivNum: " + ivNum + ")");
        }
    }

    public static void main(String[] args) {
        PokemonMethod1 pkmRNG = new PokemonMethod1(0x8c6b6616);
        System.out.printf("IVs: %d HP/%d Atk/%d Def/%d SpA/%d SpD/%d Spd\nNature: %s\nPID: 0x%08x",
            pkmRNG.hp, pkmRNG.atk, pkmRNG.def, pkmRNG.spa, pkmRNG.spd, pkmRNG.spe, pkmRNG.nature, pkmRNG.pid);
    }
}
