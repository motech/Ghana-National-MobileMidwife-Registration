package org.motechghana;

import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.joda.time.DateTime;
import org.motechproject.dao.MotechBaseRepository;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;

import java.util.List;

public class AllMMEnrollments extends MotechBaseRepository<MobileMidwifeEnrollment> {

    @Autowired
    public AllMMEnrollments(@Qualifier("couchDbConnector") CouchDbConnector db) {
        super(MobileMidwifeEnrollment.class, db);
    }

    @View(name = "find_by_patientId", map = "function(doc){ if(doc.type === 'MobileMidwifeEnrollment') emit([doc.patientId, doc.enrollmentDateTime, doc.active], doc) }")
    public List<MobileMidwifeEnrollment> findEnrollments(String patientId, DateTime enrollmentDateTime, boolean active) {
        ViewQuery viewQuery = createQuery("find_by_patientId").key(ComplexKey.of(patientId, enrollmentDateTime, active)).includeDocs(true);
        return db.queryView(viewQuery, MobileMidwifeEnrollment.class);
    }

    public void create(MobileMidwifeEnrollment enrollment) {
        super.add(enrollment);
    }

}
