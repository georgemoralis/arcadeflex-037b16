/*
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b16.cpu.m6800;

import static gr.codebb.arcadeflex.v037b16.cpu.m6800.m6800ops.*;

public class m6800tbl {

    static opcode[] m6800_insn = {
        illegal, nop, illegal, illegal, illegal, illegal, tap, tpa,
        inx, dex, clv, sev, clc, sec, cli, sei,
        sba, cba, illegal, illegal, illegal, illegal, tab, tba,
        illegal, daa, illegal, aba, illegal, illegal, illegal, illegal,
        bra, brn, bhi, bls, bcc, bcs, bne, beq,
        bvc, bvs, bpl, bmi, bge, blt, bgt, ble,
        tsx, ins, pula, pulb, des, txs, psha, pshb,
        illegal, rts, illegal, rti, illegal, illegal, wai, swi,
        nega, illegal, illegal, coma, lsra, illegal, rora, asra,
        asla, rola, deca, illegal, inca, tsta, illegal, clra,
        negb, illegal, illegal, comb, lsrb, illegal, rorb, asrb,
        aslb, rolb, decb, illegal, incb, tstb, illegal, clrb,
        neg_ix, illegal, illegal, com_ix, lsr_ix, illegal, ror_ix, asr_ix,
        asl_ix, rol_ix, dec_ix, illegal, inc_ix, tst_ix, jmp_ix, clr_ix,
        neg_ex, illegal, illegal, com_ex, lsr_ex, illegal, ror_ex, asr_ex,
        asl_ex, rol_ex, dec_ex, illegal, inc_ex, tst_ex, jmp_ex, clr_ex,
        suba_im, cmpa_im, sbca_im, illegal, anda_im, bita_im, lda_im, sta_im,
        eora_im, adca_im, ora_im, adda_im, cmpx_im, bsr, lds_im, sts_im,
        suba_di, cmpa_di, sbca_di, illegal, anda_di, bita_di, lda_di, sta_di,
        eora_di, adca_di, ora_di, adda_di, cmpx_di, jsr_di, lds_di, sts_di,
        suba_ix, cmpa_ix, sbca_ix, illegal, anda_ix, bita_ix, lda_ix, sta_ix,
        eora_ix, adca_ix, ora_ix, adda_ix, cmpx_ix, jsr_ix, lds_ix, sts_ix,
        suba_ex, cmpa_ex, sbca_ex, illegal, anda_ex, bita_ex, lda_ex, sta_ex,
        eora_ex, adca_ex, ora_ex, adda_ex, cmpx_ex, jsr_ex, lds_ex, sts_ex,
        subb_im, cmpb_im, sbcb_im, illegal, andb_im, bitb_im, ldb_im, stb_im,
        eorb_im, adcb_im, orb_im, addb_im, illegal, illegal, ldx_im, stx_im,
        subb_di, cmpb_di, sbcb_di, illegal, andb_di, bitb_di, ldb_di, stb_di,
        eorb_di, adcb_di, orb_di, addb_di, illegal, illegal, ldx_di, stx_di,
        subb_ix, cmpb_ix, sbcb_ix, illegal, andb_ix, bitb_ix, ldb_ix, stb_ix,
        eorb_ix, adcb_ix, orb_ix, addb_ix, illegal, illegal, ldx_ix, stx_ix,
        subb_ex, cmpb_ex, sbcb_ex, illegal, andb_ex, bitb_ex, ldb_ex, stb_ex,
        eorb_ex, adcb_ex, orb_ex, addb_ex, illegal, illegal, ldx_ex, stx_ex
    };

    static opcode[] m6803_insn = {
        illegal, nop, illegal, illegal, lsrd, asld, tap, tpa,
        inx, dex, clv, sev, clc, sec, cli, sei,
        sba, cba, illegal, illegal, illegal, illegal, tab, tba,
        illegal, daa, illegal, aba, illegal, illegal, illegal, illegal,
        bra, brn, bhi, bls, bcc, bcs, bne, beq,
        bvc, bvs, bpl, bmi, bge, blt, bgt, ble,
        tsx, ins, pula, pulb, des, txs, psha, pshb,
        pulx, rts, abx, rti, pshx, mul, wai, swi,
        nega, illegal, illegal, coma, lsra, illegal, rora, asra,
        asla, rola, deca, illegal, inca, tsta, illegal, clra,
        negb, illegal, illegal, comb, lsrb, illegal, rorb, asrb,
        aslb, rolb, decb, illegal, incb, tstb, illegal, clrb,
        neg_ix, illegal, illegal, com_ix, lsr_ix, illegal, ror_ix, asr_ix,
        asl_ix, rol_ix, dec_ix, illegal, inc_ix, tst_ix, jmp_ix, clr_ix,
        neg_ex, illegal, illegal, com_ex, lsr_ex, illegal, ror_ex, asr_ex,
        asl_ex, rol_ex, dec_ex, illegal, inc_ex, tst_ex, jmp_ex, clr_ex,
        suba_im, cmpa_im, sbca_im, subd_im, anda_im, bita_im, lda_im, sta_im,
        eora_im, adca_im, ora_im, adda_im, cpx_im, bsr, lds_im, sts_im,
        suba_di, cmpa_di, sbca_di, subd_di, anda_di, bita_di, lda_di, sta_di,
        eora_di, adca_di, ora_di, adda_di, cpx_di, jsr_di, lds_di, sts_di,
        suba_ix, cmpa_ix, sbca_ix, subd_ix, anda_ix, bita_ix, lda_ix, sta_ix,
        eora_ix, adca_ix, ora_ix, adda_ix, cpx_ix, jsr_ix, lds_ix, sts_ix,
        suba_ex, cmpa_ex, sbca_ex, subd_ex, anda_ex, bita_ex, lda_ex, sta_ex,
        eora_ex, adca_ex, ora_ex, adda_ex, cpx_ex, jsr_ex, lds_ex, sts_ex,
        subb_im, cmpb_im, sbcb_im, addd_im, andb_im, bitb_im, ldb_im, stb_im,
        eorb_im, adcb_im, orb_im, addb_im, ldd_im, std_im, ldx_im, stx_im,
        subb_di, cmpb_di, sbcb_di, addd_di, andb_di, bitb_di, ldb_di, stb_di,
        eorb_di, adcb_di, orb_di, addb_di, ldd_di, std_di, ldx_di, stx_di,
        subb_ix, cmpb_ix, sbcb_ix, addd_ix, andb_ix, bitb_ix, ldb_ix, stb_ix,
        eorb_ix, adcb_ix, orb_ix, addb_ix, ldd_ix, std_ix, ldx_ix, stx_ix,
        subb_ex, cmpb_ex, sbcb_ex, addd_ex, andb_ex, bitb_ex, ldb_ex, stb_ex,
        eorb_ex, adcb_ex, orb_ex, addb_ex, ldd_ex, std_ex, ldx_ex, stx_ex
    };

    static opcode hd63701_insn[] = {
        trap, nop, trap, trap, lsrd, asld, tap, tpa,
        inx, dex, clv, sev, clc, sec, cli, sei,
        sba, cba, undoc1, undoc2, trap, trap, tab, tba,
        xgdx, daa, slp, aba, trap, trap, trap, trap,
        bra, brn, bhi, bls, bcc, bcs, bne, beq,
        bvc, bvs, bpl, bmi, bge, blt, bgt, ble,
        tsx, ins, pula, pulb, des, txs, psha, pshb,
        pulx, rts, abx, rti, pshx, mul, wai, swi,
        nega, trap, trap, coma, lsra, trap, rora, asra,
        asla, rola, deca, trap, inca, tsta, trap, clra,
        negb, trap, trap, comb, lsrb, trap, rorb, asrb,
        aslb, rolb, decb, trap, incb, tstb, trap, clrb,
        neg_ix, aim_ix, oim_ix, com_ix, lsr_ix, eim_ix, ror_ix, asr_ix,
        asl_ix, rol_ix, dec_ix, tim_ix, inc_ix, tst_ix, jmp_ix, clr_ix,
        neg_ex, aim_di, oim_di, com_ex, lsr_ex, eim_di, ror_ex, asr_ex,
        asl_ex, rol_ex, dec_ex, tim_di, inc_ex, tst_ex, jmp_ex, clr_ex,
        suba_im, cmpa_im, sbca_im, subd_im, anda_im, bita_im, lda_im, sta_im,
        eora_im, adca_im, ora_im, adda_im, cpx_im, bsr, lds_im, sts_im,
        suba_di, cmpa_di, sbca_di, subd_di, anda_di, bita_di, lda_di, sta_di,
        eora_di, adca_di, ora_di, adda_di, cpx_di, jsr_di, lds_di, sts_di,
        suba_ix, cmpa_ix, sbca_ix, subd_ix, anda_ix, bita_ix, lda_ix, sta_ix,
        eora_ix, adca_ix, ora_ix, adda_ix, cpx_ix, jsr_ix, lds_ix, sts_ix,
        suba_ex, cmpa_ex, sbca_ex, subd_ex, anda_ex, bita_ex, lda_ex, sta_ex,
        eora_ex, adca_ex, ora_ex, adda_ex, cpx_ex, jsr_ex, lds_ex, sts_ex,
        subb_im, cmpb_im, sbcb_im, addd_im, andb_im, bitb_im, ldb_im, stb_im,
        eorb_im, adcb_im, orb_im, addb_im, ldd_im, std_im, ldx_im, stx_im,
        subb_di, cmpb_di, sbcb_di, addd_di, andb_di, bitb_di, ldb_di, stb_di,
        eorb_di, adcb_di, orb_di, addb_di, ldd_di, std_di, ldx_di, stx_di,
        subb_ix, cmpb_ix, sbcb_ix, addd_ix, andb_ix, bitb_ix, ldb_ix, stb_ix,
        eorb_ix, adcb_ix, orb_ix, addb_ix, ldd_ix, std_ix, ldx_ix, stx_ix,
        subb_ex, cmpb_ex, sbcb_ex, addd_ex, andb_ex, bitb_ex, ldb_ex, stb_ex,
        eorb_ex, adcb_ex, orb_ex, addb_ex, ldd_ex, std_ex, ldx_ex, stx_ex
    };
    static opcode[] nsc8105_insn = {
        illegal, illegal, nop, illegal, illegal, tap, illegal, tpa,
        inx, clv, dex, sev, clc, cli, sec, sei,
        sba, illegal, cba, illegal, illegal, tab, illegal, tba,
        illegal, illegal, daa, aba, illegal, illegal, illegal, illegal,
        bra, bhi, brn, bls, bcc, bne, bcs, beq,
        bvc, bpl, bvs, bmi, bge, bgt, blt, ble,
        tsx, pula, ins, pulb, des, psha, txs, pshb,
        illegal, illegal, rts, rti, illegal, wai, illegal, swi,
        suba_im, sbca_im, cmpa_im, illegal, anda_im, lda_im, bita_im, sta_im,
        eora_im, ora_im, adca_im, adda_im, cmpx_im, lds_im, bsr, sts_im,
        suba_di, sbca_di, cmpa_di, illegal, anda_di, lda_di, bita_di, sta_di,
        eora_di, ora_di, adca_di, adda_di, cmpx_di, lds_di, jsr_di, sts_di,
        suba_ix, sbca_ix, cmpa_ix, illegal, anda_ix, lda_ix, bita_ix, sta_ix,
        eora_ix, ora_ix, adca_ix, adda_ix, cmpx_ix, lds_ix, jsr_ix, sts_ix,
        suba_ex, sbca_ex, cmpa_ex, illegal, anda_ex, lda_ex, bita_ex, sta_ex,
        eora_ex, ora_ex, adca_ex, adda_ex, cmpx_ex, lds_ex, jsr_ex, sts_ex,
        nega, illegal, illegal, coma, lsra, rora, illegal, asra,
        asla, deca, rola, illegal, inca, illegal, tsta, clra,
        negb, illegal, illegal, comb, lsrb, rorb, illegal, asrb,
        aslb, decb, rolb, illegal, incb, illegal, tstb, clrb,
        neg_ix, illegal, illegal, com_ix, lsr_ix, ror_ix, illegal, asr_ix,
        asl_ix, dec_ix, rol_ix, illegal, inc_ix, jmp_ix, tst_ix, clr_ix,
        neg_ex, illegal, illegal, com_ex, lsr_ex, ror_ex, illegal, asr_ex,
        asl_ex, dec_ex, rol_ex, illegal, inc_ex, jmp_ex, tst_ex, clr_ex,
        subb_im, sbcb_im, cmpb_im, illegal, andb_im, ldb_im, bitb_im, stb_im,
        eorb_im, orb_im, adcb_im, addb_im, illegal, ldx_im, illegal, stx_im,
        subb_di, sbcb_di, cmpb_di, illegal, andb_di, ldb_di, bitb_di, stb_di,
        eorb_di, orb_di, adcb_di, addb_di, illegal, ldx_di, illegal, stx_di,
        subb_ix, sbcb_ix, cmpb_ix, illegal, andb_ix, ldb_ix, bitb_ix, stb_ix,
        eorb_ix, orb_ix, adcb_ix, addb_ix, illegal, ldx_ix, illegal, stx_ix,
        subb_ex, sbcb_ex, cmpb_ex, illegal, andb_ex, ldb_ex, bitb_ex, stb_ex,
        eorb_ex, orb_ex, adcb_ex, addb_ex, addx_ex, ldx_ex, illegal, stx_ex
    };
}
