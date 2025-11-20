package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class ScheduledJobTest {

    @Test
    public void testConstructorWithValidJson() {
        // Arrange: Create a valid JSON string
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleDeviceMethod\",\n" +
                "    \"queryCondition\":\"DeviceId IN ['new_device']\",\n" +
                "    \"failureReason\":\"Valid failure reason\",\n" +
                "    \"statusMessage\":\"Valid status message\",\n" +
                "    \"deviceId\":\"ValidDeviceId\",\n" +
                "    \"parentJobId\":\"ValidParentJobId\",\n" +
                "    \"maxExecutionTimeSeconds\":120\n" +
                "}";

        // Act: Create ScheduledJob instance
        ScheduledJob job = new ScheduledJob(json);

        // Assert: Verify fields are populated correctly
        assertEquals("jobName", job.getJobId());
        assertEquals("DeviceId IN ['new_device']", job.getQueryCondition());
        assertEquals(ScheduledJobType.scheduleDeviceMethod, job.getJobType());
        assertEquals(ScheduledJobStatus.enqueued, job.getJobStatus());
    }

    @Test
    public void testConstructorWithCloudToDeviceMethod() {
        // Arrange: JSON with cloudToDeviceMethod
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleDeviceMethod\",\n" +
                "    \"cloudToDeviceMethod\":\n" +
                "    {\n" +
                "        \"methodName\":\"reboot\",\n" +
                "        \"responseTimeoutInSeconds\":200,\n" +
                "        \"connectTimeoutInSeconds\":5,\n" +
                "        \"payload\":{\"Tag1\":100}\n" +
                "    },\n" +
                "    \"maxExecutionTimeSeconds\":120\n" +
                "}";

        // Act
        ScheduledJob job = new ScheduledJob(json);

        // Assert
        assertNotNull(job.getCloudToDeviceMethod());
    }

    @Test
    public void testGetCloudToDeviceMethodWithFailedStatus() {
        // Arrange: Create a valid JSON string with status "failed"
        String json =
                "{\"deviceId\":\"validDeviceID\"," +
                "\"jobId\":\"DHCMD798400e8-6d6c-44a5-b0ec-a790ad9705de\"," +
                "\"jobType\":\"scheduleDeviceMethod\"," +
                "\"status\":\"failed\"," +
                "\"startTimeUtc\":\"2017-07-08T00:02:25.0556Z\"," +
                "\"endTimeUtc\":\"2017-07-08T00:04:05.0556Z\"," +
                "\"createdDateTimeUtc\":\"2017-07-08T00:02:31.6162976Z\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-08T00:04:05.0736166Z\"," +
                "\"outcome\":{}," +
                "\"error\":{" +
                    "\"code\":\"JobRunPreconditionFailed\"," +
                "   \"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                    "}" +
                "}";

        // Act
        ScheduledJob job = new ScheduledJob(json);

        // Assert
        assertEquals(null, job.getCloudToDeviceMethod());
        assertEquals(null, job.getOutcomeResult());
        assertNotNull(job.getError());
    }

    @Test
    public void testConstructorWithTwinUpdate() {
        // Arrange: JSON with twin update
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"updateTwin\":\n" +
                "    {\n" +
                "        \"deviceId\":\"new_device\",\n" +
                "        \"etag\":\"etag123\",\n" +
                "        \"tags\":{\"Tag1\":100},\n" +
                "        \"properties\":{\"desired\":{},\"reported\":{}}\n" +
                "    },\n" +
                "    \"queryCondition\":\"DeviceId IN ['new_device']\",\n" +
                "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"failureReason\":\"Valid failure reason\",\n" +
                "    \"statusMessage\":\"Valid status message\",\n" +
                "    \"deviceId\":\"ValidDeviceId\",\n" +
                "    \"parentJobId\":\"ValidParentJobId\",\n" +
                "    \"deviceJobStatistics\": {\n" +
                "        \"deviceCount\": 1,\n" +
                "        \"failedCount\": 2,\n" +
                "        \"succeededCount\": 3,\n" +
                "        \"runningCount\": 4,\n" +
                "        \"pendingCount\": 5\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // Act
        ScheduledJob job = new ScheduledJob(json);

        // Assert
        assertNotNull(job.getUpdateTwin());
        assertEquals("new_device", job.getUpdateTwin().getDeviceId());
        assertEquals("etag123", job.getUpdateTwin().getETag());
        assertTrue(job.getUpdateTwin().getTags().containsKey("Tag1"));
    }

}
