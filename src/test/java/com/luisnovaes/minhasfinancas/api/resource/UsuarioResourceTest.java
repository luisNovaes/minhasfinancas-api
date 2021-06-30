package com.luisnovaes.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luisnovaes.minhasfinancas.api.dto.UsuarioDTO;
import com.luisnovaes.minhasfinancas.exception.ErroAutenticacao;
import com.luisnovaes.minhasfinancas.exception.RegraNegocioException;
import com.luisnovaes.minhasfinancas.model.entity.Usuario;
import com.luisnovaes.minhasfinancas.service.LancamentoService;
import com.luisnovaes.minhasfinancas.service.UsuarioService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = UsuarioResource.class)//testar controllers - sibir somente contexto do usuário
@AutoConfigureMockMvc
public class UsuarioResourceTest {

	static final String API = "/api/usuarios";
	static final MediaType JSON = MediaType.APPLICATION_JSON;

	@Autowired
	MockMvc mvc;

	@MockBean
	UsuarioService service;

	@MockBean
	LancamentoService lancamentoService;

	@Test
	public void deveAutenticarUmUsuario() throws Exception {
		// cenario
		String email = "usuario@email.com";
		String senha = "123";

		UsuarioDTO dto = new UsuarioDTO();
		dto.setEmail(email);
		dto.setSenha(senha);
		
		Usuario usuario = new Usuario();
		usuario.setId(1l);
		usuario.setEmail(email);
		usuario.setSenha(senha);		
		
		Mockito.when(service.autenticar(email, senha)).thenReturn(usuario);
		
		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
													.post(API.concat("/autenticar"))
													.accept(JSON)
													.contentType(JSON)
													.content(json);

		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
				.andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()))

		;

	}

	@Test
	public void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception {
		// cenario
		String email = "usuario@email.com";
		String senha = "123";

		UsuarioDTO dto = new UsuarioDTO();
		dto.setEmail(email);
		dto.setSenha(senha);
				
		Mockito.when(service.autenticar(email, senha)).thenThrow(ErroAutenticacao.class);

		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
														.post(API.concat("/autenticar"))
														.accept(JSON)
														.contentType(JSON)
														.content(json);

		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());

		

	}

	@Test
	public void deveCriarUmNovoUsuario() throws Exception {
		// cenario
		String email = "usuario@email.com";
		String senha = "123";

		UsuarioDTO dto = new UsuarioDTO();
			dto.setEmail("usuario@email.com");
			dto.setSenha("123");	
		
		Usuario usuario = new Usuario();
			usuario.setId(1l);
			usuario.setEmail(email);	
			usuario.setSenha(senha);

		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
														.post(API)
														.accept(JSON)
														.contentType(JSON)
														.content(json);

		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
				.andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));

	}

	@Test
	public void deveRetornarBadRequestAoTentarCriarUmUsuarioInvalido() throws Exception {
		// cenario
		String email = "usuario@email.com";
		String senha = "123";

		UsuarioDTO dto = new UsuarioDTO();
			dto.setEmail("usuario@email.com");
			dto.setSenha("123");

		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenThrow(RegraNegocioException.class);
		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
															.post(API)
															.accept(JSON)
															.contentType(JSON)
															.content(json);

		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());


	}

	@Test
	public void deveObterOSaldoDoUsuario() throws Exception {

		// cenário

		BigDecimal saldo = BigDecimal.valueOf(10);
		
		Usuario usuario = new Usuario();
			usuario.setId(1l);
			usuario.setEmail("usuario@email.com");
			usuario.setSenha("123");
		
		
		Mockito.when(service.obterPorId(1l)).thenReturn(Optional.of(usuario));
		Mockito.when(lancamentoService.obterSaldoPorUsuario(1l)).thenReturn(saldo);

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(API.concat("/1/saldo")).accept(JSON)
				.contentType(JSON);
		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("10"));

	}

	@Test
	public void deveRetornarResourceNotFoundQuandoUsuarioNaoExisteParaObterOSaldo() throws Exception {

		// cenário
		Mockito.when(service.obterPorId(1l)).thenReturn(Optional.empty());

		// execucao e verificacao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(API.concat("/1/saldo")).accept(JSON)
				.contentType(JSON);
		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());

	}

}
