package com.literature.knowledge.service;

import com.literature.knowledge.config.HistoryGuardProperties;
import com.literature.knowledge.config.HistoryGuardProperties.Conflict.ResolutionStrategy;
import com.literature.knowledge.entity.SourceConflict;
import com.literature.knowledge.mapper.SourceConflictMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConflictResolutionServiceTest {

    @Mock
    private SourceConflictMapper conflictMapper;

    private HistoryGuardProperties properties;
    private ConflictResolutionService service;

    @BeforeEach
    void setUp() {
        properties = new HistoryGuardProperties();
        service = new ConflictResolutionService(conflictMapper, properties);
    }

    private SourceConflict buildTestConflict() {
        SourceConflict conflict = new SourceConflict();
        conflict.setTopic("赤壁之战曹军规模");

        SourceConflict.Perspective p1 = new SourceConflict.Perspective();
        p1.setSource("《三国志·吴主传》");
        p1.setClaim("曹公入荆州，刘琮举众降，讨虏大破之");
        p1.setReliability(1.0);

        SourceConflict.Perspective p2 = new SourceConflict.Perspective();
        p2.setSource("《资治通鉴》");
        p2.setClaim("操引军从江陵将顺江东下，诸葛亮谓刘备曰：操自来送死");
        p2.setReliability(0.95);

        SourceConflict.Perspective p3 = new SourceConflict.Perspective();
        p3.setSource("《三国演义》");
        p3.setClaim("操统百万雄师南下");
        p3.setReliability(0.6);

        conflict.setPerspectives(Arrays.asList(p1, p2, p3));
        return conflict;
    }

    @Test
    void formatConflictResponse_MultiView_ShouldShowAllPerspectives() {
        properties.getConflict().setResolutionStrategy(ResolutionStrategy.MULTI_VIEW);
        properties.getConflict().setShowSourceReliability(true);
        properties.getConflict().setMaxPerspectives(3);
        SourceConflict conflict = buildTestConflict();

        String result = service.formatConflictResponse(conflict);

        assertTrue(result.contains("《三国志·吴主传》"));
        assertTrue(result.contains("《资治通鉴》"));
        assertTrue(result.contains("《三国演义》"));
        assertTrue(result.contains("100%")); // 1.0 reliability
        assertTrue(result.contains("60%")); // 0.6 reliability
    }

    @Test
    void formatConflictResponse_SingleAuthority_ShouldShowOnlyBest() {
        properties.getConflict().setResolutionStrategy(ResolutionStrategy.SINGLE_AUTHORITY);
        SourceConflict conflict = buildTestConflict();

        String result = service.formatConflictResponse(conflict);

        assertTrue(result.contains("《三国志·吴主传》")); // highest reliability
        assertFalse(result.contains("《三国演义》")); // lowest reliability excluded
    }

    @Test
    void formatConflictResponse_ScholarlyConsensus_WithConsensus() {
        properties.getConflict().setResolutionStrategy(ResolutionStrategy.SCHOLARLY_CONSENSUS);
        SourceConflict conflict = buildTestConflict();
        conflict.setResolutionType(SourceConflict.ResolutionType.CONSENSUS);
        conflict.setResolution("现代学者普遍认为曹军实际兵力约二十余万");

        String result = service.formatConflictResponse(conflict);

        assertTrue(result.contains("学术共识"));
        assertTrue(result.contains("二十余万"));
    }

    @Test
    void formatConflictResponse_ScholarlyConsensus_Unresolved() {
        properties.getConflict().setResolutionStrategy(ResolutionStrategy.SCHOLARLY_CONSENSUS);
        SourceConflict conflict = buildTestConflict();
        conflict.setResolutionType(SourceConflict.ResolutionType.UNRESOLVED);
        conflict.setResolution(null);

        String result = service.formatConflictResponse(conflict);

        assertTrue(result.contains("尚无学术共识"));
    }

    @Test
    void formatConflictResponse_MultiView_LimitPerspectives() {
        properties.getConflict().setResolutionStrategy(ResolutionStrategy.MULTI_VIEW);
        properties.getConflict().setMaxPerspectives(2); // only top 2
        SourceConflict conflict = buildTestConflict();

        String result = service.formatConflictResponse(conflict);

        assertTrue(result.contains("《三国志·吴主传》")); // reliability 1.0
        assertTrue(result.contains("《资治通鉴》")); // reliability 0.95
        assertFalse(result.contains("《三国演义》")); // reliability 0.6 → excluded
    }
}
