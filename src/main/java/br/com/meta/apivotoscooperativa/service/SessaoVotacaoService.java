package br.com.meta.apivotoscooperativa.service;

import br.com.meta.apivotoscooperativa.exception.PautaNotFoundException;
import br.com.meta.apivotoscooperativa.model.Pauta;
import br.com.meta.apivotoscooperativa.model.SessaoVotacao;
import br.com.meta.apivotoscooperativa.repository.SessaoVotacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@Transactional
public class SessaoVotacaoService {

    @Autowired
    SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    PautaService pautaService;

    public Iterable<SessaoVotacao> listAllSessoes(){
        return sessaoVotacaoRepository.findAll();
    }

    public SessaoVotacao saveSessaoVotacao(SessaoVotacao sessaoVotacao){
        return sessaoVotacaoRepository.save(sessaoVotacao);
    }

    public SessaoVotacao findById(Integer id) {
        return sessaoVotacaoRepository.findById(id).orElseThrow(
                () -> new PautaNotFoundException("SessaoVotacao with id " + id + " was not found.")
        );
    }

    @Transactional
    public ResponseEntity<Object> createSessao(Pauta pauta, SessaoVotacao sessao) {
        try {
            sessao.setPauta(pauta);
            pautaService.addSessaoVotacao(pauta, sessao);
            return ResponseEntity.status(HttpStatus.CREATED).body("Sessao " + sessao.getId() + " adicionada com sucesso.\nId da pauta: " + pauta.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> updateSessao(Integer sessaoId, SessaoVotacao sessao) {
        try {
            sessao.setIsOpen();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int) sessao.getDuration().toMinutes());
            sessao.setExpireAt(calendar.getTime());
            saveSessaoVotacao(sessao);
            return ResponseEntity.ok("SessaoVotacao with id " + sessaoId + " is now open.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in change SessaoVotacao " + sessaoId + " status.");
        }
    }

    public boolean isSessaoOpen(SessaoVotacao sessao) {
        return sessao.getIsOpen();
    }

    public boolean isSessaoExpired(SessaoVotacao sessao) {
        return sessao.isExpired();
    }

    public void addVoto(SessaoVotacao sessao, boolean voto) {
        if (voto) {
            sessao.setVotosSim();
        } else {
            sessao.setVotosNao();
        }
        sessao.setVotosTotal();
        saveSessaoVotacao(sessao);
    }
}
