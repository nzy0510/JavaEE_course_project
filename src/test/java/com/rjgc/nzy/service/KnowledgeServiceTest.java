package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.mapper.KnowledgeAtomMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeServiceTest {

    private final KnowledgeAtomMapper mapper = mock(KnowledgeAtomMapper.class);
    private final KnowledgeService service = new KnowledgeService(mapper);

    @Test
    void addNormalizesBlankTagsAndSetsActiveStatus() {
        KnowledgeAtom atom = new KnowledgeAtom();
        atom.setSubject("Spring Bean 生命周期");
        atom.setCategory("Spring");
        atom.setTags("");
        atom.setPrinciples("Bean 生命周期包括实例化、属性填充、初始化和销毁。");

        service.add(atom);

        ArgumentCaptor<KnowledgeAtom> captor = ArgumentCaptor.forClass(KnowledgeAtom.class);
        verify(mapper).insert(captor.capture());
        KnowledgeAtom saved = captor.getValue();
        assertThat(saved.getTags()).isNull();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getDifficulty()).isEqualTo("中等");
    }

    @Test
    void addRejectsInvalidTagsJson() {
        KnowledgeAtom atom = new KnowledgeAtom();
        atom.setSubject("Spring Bean 生命周期");
        atom.setCategory("Spring");
        atom.setTags("Spring,Bean");
        atom.setPrinciples("Bean 生命周期包括实例化、属性填充、初始化和销毁。");

        assertThatThrownBy(() -> service.add(atom))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("标签必须是 JSON 数组格式");
    }

    @Test
    void addRejectsMissingRequiredFieldsBeforeDatabaseInsert() {
        KnowledgeAtom atom = new KnowledgeAtom();
        atom.setCategory("Spring");
        atom.setPrinciples("内容");

        assertThatThrownBy(() -> service.add(atom))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("标题不能为空");
    }
}
