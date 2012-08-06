package org.motechghana;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.motechproject.ghana.national.domain.mobilemidwife.Language;
import org.motechproject.ghana.national.domain.mobilemidwife.LearnedFrom;
import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.domain.mobilemidwife.PhoneOwnership;
import org.motechproject.ghana.national.domain.mobilemidwife.ServiceType;
import org.motechproject.model.DayOfWeek;
import org.motechproject.model.Time;
import org.motechproject.util.DateUtil;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Migrate {
    public static void main(String[] args)
            throws IOException, ParseException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("aContext.xml");
        AllMMEnrollments allMMEnrollments = (AllMMEnrollments) ctx.getBean("allMMEnrollments");
        Migrate migrate = new Migrate();
//        List<MobileMidwifeEnrollment> mobileMidwifeEnrollments = migrate.newRegistrations("migration.csv");
//        int i = 0;
//        for (MobileMidwifeEnrollment mobileMidwifeEnrollment : mobileMidwifeEnrollments) {
//            allMMEnrollments.create(mobileMidwifeEnrollment);
//            System.out.println("migrated..." + ++i);
//        }
        migrate.updateRegistrations("migrate.csv", allMMEnrollments);

    }

    private void updateRegistrations(String fileName, AllMMEnrollments allMMEnrollments) throws IOException, ParseException {
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName)));
        int i = 0;
        for (String[] registrationDetails : reader.readAll()) {
            List<MobileMidwifeEnrollment> enrollments = allMMEnrollments.findEnrollments(registrationDetails[9], DateUtil.newDateTime(new SimpleDateFormat("dd-MMM-yy").parse(registrationDetails[3])), true);
            if (enrollments.size() != 1) {
                throw new RuntimeException("some error for patient id: ");
            } else {
                MobileMidwifeEnrollment mobileMidwifeEnrollment = enrollments.get(0);
                if (mobileMidwifeEnrollment.getServiceType().equals(ServiceType.CHILD_CARE) && Integer.parseInt(mobileMidwifeEnrollment.getMessageStartWeek()) <= 52) {
                    System.out.println("changing message start week for : " + mobileMidwifeEnrollment.getPatientId() + " : " + mobileMidwifeEnrollment.getMessageStartWeek() + " -> " + mobileMidwifeEnrollment.getMessageStartWeek() + 40);
                    mobileMidwifeEnrollment.setMessageStartWeek(mobileMidwifeEnrollment.getMessageStartWeek() + 40);
                    allMMEnrollments.update(mobileMidwifeEnrollment);
                    System.out.println("migrated " + ++i);
                }

            }
        }
    }

    private List<MobileMidwifeEnrollment> newRegistrations(String fileName) throws IOException, ParseException {
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName)));
        List<MobileMidwifeEnrollment> mobileMidwifeEnrollments = new ArrayList<MobileMidwifeEnrollment>();
        for (String[] registrationDetails : reader.readAll()) {
            MobileMidwifeEnrollment mobileMidwifeEnrollment = new MobileMidwifeEnrollment(DateUtil.newDateTime(new SimpleDateFormat("dd-MMM-yy").parse(registrationDetails[3])));
            mobileMidwifeEnrollment.setActive(boolFromString(registrationDetails[0]));
            mobileMidwifeEnrollment.setConsent(boolFromString(registrationDetails[1]));
            DayOfWeek dayOfWeek = registrationDetails[2].isEmpty() ? null : DayOfWeek.valueOf(registrationDetails[2].toUpperCase().substring(0, 1) + registrationDetails[2].toLowerCase().substring(1));
            mobileMidwifeEnrollment.setDayOfWeek(dayOfWeek);
            mobileMidwifeEnrollment.setFacilityId(registrationDetails[4]);
            mobileMidwifeEnrollment.setLanguage(Language.valueOf(registrationDetails[5].toUpperCase()));
            if ("NULL".equals(registrationDetails[6]))
                mobileMidwifeEnrollment.setLearnedFrom(null);
            else
                mobileMidwifeEnrollment.setLearnedFrom(LearnedFrom.valueOf(registrationDetails[6]));
            mobileMidwifeEnrollment.setMedium(Medium.valueOf(registrationDetails[7]));
            mobileMidwifeEnrollment.setMessageStartWeek(registrationDetails[8]);
            mobileMidwifeEnrollment.setPatientId(registrationDetails[9]);
            mobileMidwifeEnrollment.setPhoneNumber("0" + registrationDetails[10]);
            mobileMidwifeEnrollment.setPhoneOwnership(PhoneOwnership.valueOf(registrationDetails[11]));
            mobileMidwifeEnrollment.setServiceType(ServiceType.valueOf(registrationDetails[12]));
            mobileMidwifeEnrollment.setStaffId(registrationDetails[13]);
            Time timeOfDay = registrationDetails[14].isEmpty() ? null : new Time(Integer.parseInt(registrationDetails[14]), Integer.parseInt(registrationDetails[15]));
            mobileMidwifeEnrollment.setTimeOfDay(timeOfDay);
            mobileMidwifeEnrollments.add(mobileMidwifeEnrollment);
        }
        return mobileMidwifeEnrollments;
    }

    private boolean boolFromString(String boolString) {
        if (boolString.toUpperCase().equals("TRUE"))
            return true;
        else if (boolString.toUpperCase().equals("FALSE"))
            return false;
        else
            throw new RuntimeException("invalid boolean string, " + boolString);
    }
}