package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.mapper.KnowledgeAtomMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void searchForAiWithScoresRanksTopMatchesAndFiltersLowScores() {
        KnowledgeAtom lifecycle = atom("Spring Bean 生命周期", "[\"Spring\",\"Bean\"]",
                "Bean 生命周期包括实例化、属性填充、初始化和销毁。");
        KnowledgeAtom dependencyInjection = atom("依赖注入", "[\"Spring\",\"DI\"]",
                "依赖注入由容器负责创建和注入对象依赖。");
        KnowledgeAtom mysql = atom("MySQL 索引", "[\"MySQL\"]",
                "索引可以提升查询效率。");
        KnowledgeAtom java = atom("Java 集合", "[\"Java\"]",
                "ArrayList 基于动态数组。");
        when(mapper.selectList(any())).thenReturn(List.of(java, mysql, dependencyInjection, lifecycle));

        List<KnowledgeSearchResult> results = service.searchForAiWithScores("Spring 容器 Bean 初始化流程怎么答？", 5);

        assertThat(results).extracting(result -> result.getAtom().getSubject())
                .containsExactly("Spring Bean 生命周期", "依赖注入");
        assertThat(results.get(0).getScore()).isGreaterThan(results.get(1).getScore());
        assertThat(results).allSatisfy(result -> assertThat(result.getScore()).isGreaterThan(0));
    }

    @Test
    void searchForAiWithScoresMatchesNaturalQuestionAgainstSimilarTitle() {
        KnowledgeAtom slowApi = atom("接口响应慢的排查思路", "[\"接口\",\"性能\"]",
                "先确认慢接口范围，再查看日志、数据库 SQL、外部依赖和资源瓶颈。");
        when(mapper.selectList(any())).thenReturn(List.of(slowApi));

        List<KnowledgeSearchResult> results = service.searchForAiWithScores("接口响应慢该如何排查？", 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAtom().getSubject()).isEqualTo("接口响应慢的排查思路");
    }

    private KnowledgeAtom atom(String subject, String tags, String principles) {
        KnowledgeAtom atom = new KnowledgeAtom();
        atom.setSubject(subject);
        atom.setTags(tags);
        atom.setPrinciples(principles);
        atom.setStatus("ACTIVE");
        return atom;
    }
}
