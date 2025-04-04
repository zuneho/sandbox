package io.github.zuneho.domain.common.util.excel;


import io.github.zuneho.domain.common.annotation.SimpleExcelField;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleExcelUtil {

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_SHEET_NAME = "report";
    private static final String SUFFIX = ".xlsx";

    private static final String DEFAULT_EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=\"%s\";";

    private SimpleExcelUtil() {
    }

    public static <T> ResponseEntity<Resource> getDownloadExcelResponse(List<T> sources, String fileName) throws IllegalArgumentException {
        return getDownloadExcelResponse(sources, fileName, "");
    }

    public static <T> ResponseEntity<Resource> getDownloadExcelResponse(List<T> sources, String fileName, String passWord) throws IllegalArgumentException {
        byte[] excelByte = getByteExcelData(sources, passWord);
        if (excelByte != null) {
            ByteArrayResource resource = new ByteArrayResource(excelByte);
            String baseFileName;
            String fileExtension = FilenameUtils.getExtension(fileName);
            if (StringUtils.isNotEmpty(fileExtension)) {
                baseFileName = FilenameUtils.getBaseName(fileName);
            } else {
                baseFileName = fileName;
            }
            String downloadFileName = baseFileName + SUFFIX;

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format(CONTENT_DISPOSITION_VALUE, downloadFileName));
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(DEFAULT_EXCEL_MEDIA_TYPE))
                    .contentLength(resource.contentLength())
                    .body(resource);
        }
        throw new IllegalArgumentException("ExcelResponseUtil create excel file error");
    }

    public static <T> void downloadExcel(HttpServletResponse response, List<T> sources, String fileName) {
        downloadExcel(response, sources, fileName, "");
    }

    public static <T> void downloadExcel(HttpServletResponse response, List<T> sources, String fileName, String passWord) {
        try {
            byte[] excelByte = getByteExcelData(sources, passWord);
            if (excelByte == null) {
                return;
            }

            String baseFileName;
            String fileExtension = FilenameUtils.getExtension(fileName);
            if (StringUtils.isNotEmpty(fileExtension)) {
                baseFileName = FilenameUtils.getBaseName(fileName);
            } else {
                baseFileName = fileName;
            }

            response.setContentType("application/download;charset=utf-8");
            response.setHeader("Content-Disposition", String.format(CONTENT_DISPOSITION_VALUE, baseFileName + SUFFIX));
            response.setHeader("Content-Transfer-Encoding", "binary");

            OutputStream servletResponseOutputStream = response.getOutputStream();
            servletResponseOutputStream.write(excelByte);
        } catch (Exception e) {
            log.error("[ExcelResponseUtil] write excel file error", e);
        }
    }

    public static <T> File getTempFiles(List<T> sources, String fileName, String passWord) throws Exception {
        if (CollectionUtils.isEmpty(sources)) {
            return null;
        }
        byte[] excelByte = getByteExcelData(sources, passWord);
        if (excelByte == null) {
            return null;
        }

        String baseFileName;
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotEmpty(fileExtension)) {
            baseFileName = FilenameUtils.getBaseName(fileName);
        } else {
            baseFileName = fileName;
        }

        Path tempFile = Files.createTempFile(baseFileName, SUFFIX);
        Files.write(tempFile, excelByte);
        return tempFile.toFile();
    }

    public static <T> byte[] getByteExcelData(List<T> sources, String passWord) {
        if (CollectionUtils.isEmpty(sources)) {
            return null;
        }
        List<Field> fields = Arrays.asList(sources.getFirst().getClass().getDeclaredFields());

        List<ExcelMetaData> metaDataList = fields.stream()
                .filter(f -> f.isAnnotationPresent(SimpleExcelField.class))
                .map(f -> ExcelMetaData.builder().order(f.getAnnotation(SimpleExcelField.class).order())
                        .headName(StringUtils.isEmpty(f.getAnnotation(SimpleExcelField.class).header()) ? f.getName() : f.getAnnotation(SimpleExcelField.class).header())
                        .columnName(f.getName())
                        .converter(f.getAnnotation(SimpleExcelField.class).convert())
                        .fieldType(f.getType()).build())
                .sorted(Comparator.comparing(ExcelMetaData::getOrder))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(metaDataList)) {
            return null;
        }

        try (POIFSFileSystem poifsFileSystem = new POIFSFileSystem();
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(DEFAULT_SHEET_NAME);

            String[] headers = metaDataList.stream().map(ExcelMetaData::getHeadName).toArray(String[]::new);
            int headerSize = headers.length;
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headerSize; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            sheet.createFreezePane(0, 1);

            Map<Class<? extends Converter<?, ?>>, Converter<?, ?>> caches = new HashMap<>();
            for (int i = 0; i < sources.size(); i++) {
                Object target = sources.get(i);
                int size = metaDataList.size();

                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < size; j++) {
                    Cell cell = row.createCell(j);
                    ExcelMetaData excelMetaData = metaDataList.get(j);
                    String filedValue = getValue(target, excelMetaData, caches);
                    cell.setCellValue(filedValue);
                }
            }
            ByteArrayOutputStream excelWriteOutputStream = new ByteArrayOutputStream();
            workbook.write(excelWriteOutputStream);

            if (StringUtils.isEmpty(passWord)) {
                return excelWriteOutputStream.toByteArray();
            } else {
                excelWriteOutputStream.close();
                InputStream excelWroteInputStream = new ByteArrayInputStream(excelWriteOutputStream.toByteArray());
                EncryptionInfo encryptionInfo = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = encryptionInfo.getEncryptor();
                encryptor.confirmPassword(passWord);
                try (OPCPackage opc = OPCPackage.open(excelWroteInputStream);
                     OutputStream encOutputStream = encryptor.getDataStream(poifsFileSystem)) {
                    opc.save(encOutputStream);
                }
                ByteArrayOutputStream encryptResultOutputStream = new ByteArrayOutputStream();
                poifsFileSystem.writeFilesystem(encryptResultOutputStream);
                return encryptResultOutputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("[ExcelResponseUtil] create excel file error", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getValue(Object target, ExcelMetaData excelMetaData, Map<Class<? extends Converter<?, ?>>, Converter<?, ?>> caches) {
        String columnName = excelMetaData.getColumnName();
        try {
            Method targetMethod = ReflectionUtils.findGetter(target, columnName);
            Object filedValue = targetMethod.invoke(target);

            if (Objects.isNull(filedValue)) {
                return StringUtils.EMPTY;
            }

            if (NoneConverter.class != excelMetaData.getConverter()) {
                Converter<Object, Object> converter = (Converter<Object, Object>) getConverterFromCache(excelMetaData, caches);
                Object convertingObject = converter.convert(filedValue);
                if (Objects.nonNull(convertingObject)) {
                    return convertingObject.toString();
                } else {
                    log.warn("[ExcelResponseUtil]  convert value is null - filed={} converter={}", excelMetaData.getColumnName(), converter.getClass());
                    return StringUtils.EMPTY;
                }
            }

            if (excelMetaData.getFieldType() == LocalDateTime.class) {
                LocalDateTime localDateTime = (LocalDateTime) filedValue;
                return localDateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));

            } else if (excelMetaData.getFieldType() == LocalDate.class) {
                LocalDate localDate = (LocalDate) filedValue;
                return localDate.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
            }

            return filedValue.toString();
        } catch (Exception e) {
            log.error("[ExcelResponseUtil] error {} get value", columnName, e);
        }
        return StringUtils.EMPTY;
    }

    private static Converter<?, ?> getConverterFromCache(ExcelMetaData excelMetaData, Map<Class<? extends Converter<?, ?>>, Converter<?, ?>> caches) {
        Converter<?, ?> instantiate = caches.get(excelMetaData.getConverter());
        if (instantiate == null) {
            instantiate = BeanUtils.instantiateClass(excelMetaData.getConverter());
            caches.put(excelMetaData.getConverter(), instantiate);
        }
        return instantiate;
    }

    @Getter
    @Builder
    private static class ExcelMetaData implements Serializable {
        private final int order;
        private final String headName;
        private final String columnName;
        private final Class<?> fieldType;
        private final Class<? extends Converter<?, ?>> converter;
    }


    public static class NoneConverter implements Converter<Object, Object> {
        @Override
        public Object convert(@Nullable Object source) {
            return source;
        }
    }


    /*
     * Copy From  org.supercsv.util.ReflectionUtils
     * CSV lib 의존성 제거 위해 별도로 떼어냄
     *
     * @author James Bassett
     * @since 2.0.0
     */
    public static class ReflectionUtils {

        public static final String GET_PREFIX = "get";
        public static final String IS_PREFIX = "is";

        // no instantiation
        private ReflectionUtils() {
        }

        /**
         * Returns the getter method associated with the object's field.
         *
         * @param object    the object
         * @param fieldName the name of the field
         * @return the getter method
         * @throws NullPointerException if object or fieldName is null
         * @throws RuntimeException     if the getter doesn't exist or is not visible
         */
        public static Method findGetter(final Object object, final String fieldName) {
            if (object == null) {
                throw new NullPointerException("object should not be null");
            } else if (fieldName == null) {
                throw new NullPointerException("fieldName should not be null");
            }

            final Class<?> clazz = object.getClass();

            // find a standard getter
            final String standardGetterName = getMethodNameForField(GET_PREFIX, fieldName);
            Method getter = findGetterWithCompatibleReturnType(standardGetterName, clazz, false);

            // if that fails, try for an isX() style boolean getter
            if (getter == null) {
                final String booleanGetterName = getMethodNameForField(IS_PREFIX, fieldName);
                getter = findGetterWithCompatibleReturnType(booleanGetterName, clazz, true);
            }

            if (getter == null) {
                throw new RuntimeException(
                        String
                                .format(
                                        "unable to find getter for field %s in class %s - check that the corresponding nameMapping element matches the field name in the bean",
                                        fieldName, clazz.getName()));
            }

            return getter;
        }

        /**
         * Helper method for findGetter() that finds a getter with the supplied name, optionally enforcing that the method
         * must have a Boolean/boolean return type. Developer note: this method could have accepted an actual return type to
         * enforce, but it was more efficient to cater for only Booleans (as they're the only type that has differently
         * named getters).
         *
         * @param getterName               the getter name
         * @param clazz                    the class
         * @param enforceBooleanReturnType if true, the method must return a Boolean/boolean, otherwise it's return type doesn't matter
         * @return the getter, or null if none is found
         */
        private static Method findGetterWithCompatibleReturnType(final String getterName, final Class<?> clazz,
                                                                 final boolean enforceBooleanReturnType) {

            for (final Method method : clazz.getMethods()) {

                if (!getterName.equalsIgnoreCase(method.getName()) || method.getParameterTypes().length != 0
                        || method.getReturnType().equals(void.class)) {
                    continue; // getter must have correct name, 0 parameters and a return type
                }

                if (!enforceBooleanReturnType || boolean.class.equals(method.getReturnType())
                        || Boolean.class.equals(method.getReturnType())) {
                    return method;
                }

            }

            return null;
        }


        /**
         * Gets the camelcase getter/setter method name for a field.
         *
         * @param prefix    the method prefix
         * @param fieldName the field name
         * @return the method name
         */
        private static String getMethodNameForField(final String prefix, final String fieldName) {
            return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
    }

}

