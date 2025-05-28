package com.artmarket.logistics_service.service;

import com.artmarket.logistics_service.DTO.*;
import com.artmarket.logistics_service.client.NovaPoshtaClient;
import com.artmarket.logistics_service.config.NovaPoshtaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NovaPoshtaService {
    private final NovaPoshtaClient novaPoshtaClient;
    private final NovaPoshtaProperties properties;

    public DocumentResponse createDelivery(NovaPoshtaDeliveryRequest request) {
        String counterpartyRef = resolveCounterparty(
                request.recipientFirstName(),
                request.recipientMiddleName(),
                request.recipientLastName(),
                request.recipientPhone(),
                request.recipientEmail()
        );


        String contactRef = getContactRecipientRef(counterpartyRef);


        String cityRef = getCityRefByName(request.cityName());
        String warehouseRef = getWarehouseRefByCityRefAndNumber(cityRef, request.warehouseNumber());


        DeliveryDocumentRequest deliveryRequest = new DeliveryDocumentRequest(
                cityRef,
                counterpartyRef,
                warehouseRef,
                contactRef,
                request.recipientPhone(),
                request.description(),
                request.cost(),
                request.weight(),
                request.volumeGeneral()
        );

        NovaPoshtaRequest npRequest = new NovaPoshtaRequest(
                properties.apiKey(),
                "InternetDocument",
                "save",
                deliveryRequest.toMethodProperties(properties)
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(npRequest);

        if (!response.success()) {
            String errorMessage = response.errors() != null ?
                    String.join(", ", response.errors()) : "Unknown error";
            throw new IllegalStateException("Failed to create delivery: " + errorMessage);
        }

        Map<String, Object> documentData = response.data().getFirst();
        return new DocumentResponse(
                (String) documentData.get("Ref"),
                convertToDouble(documentData.get("CostOnSite")),
                (String) documentData.get("EstimatedDeliveryDate"),
                (String) documentData.get("IntDocNumber")
        );
    }

    public List<Map<String, Object>> getCities(String cityName) {
        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "Address",
                "getCities",
                Map.of("FindByString", cityName)
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        return response.data();
    }

    public List<Map<String, Object>> getWarehouses(String cityName) {
        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "Address",
                "getWarehouses",
                Map.of("CityName", cityName)
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        return response.data();
    }

    public String getDocumentStatus(String documentRef) {
        Map<String, Object> methodProperties = Map.of(
                "Documents", List.of(
                        Map.of("DocumentNumber", documentRef)
                )
        );

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "TrackingDocument",
                "getStatusDocuments",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        return (String) response.data().getFirst().get("Status");
    }

    public String getCityRefByName(String cityName) {
        Map<String, Object> methodProperties = Map.of("FindByString", cityName);

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "Address",
                "getCities",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        List<Map<String, Object>> data = response.data();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("City not found: " + cityName);
        }

        return (String) data.getFirst().get("Ref");
    }

    public String getWarehouseRefByCityRefAndNumber(String cityRef, String warehouseNumber) {
        Map<String, Object> methodProperties = Map.of(
                "CityRef", cityRef,
                "FindByString", warehouseNumber
        );

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "AddressGeneral",
                "getWarehouses",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        List<Map<String, Object>> data = response.data();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Warehouse not found: " + warehouseNumber);
        }

        return (String) data.getFirst().get("Ref");
    }

    public String getContactRecipientRef(String counterpartyRef) {
        Map<String, Object> methodProperties = Map.of("Ref", counterpartyRef);

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "Counterparty",
                "getCounterpartyContactPersons",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        List<Map<String, Object>> data = response.data();

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Contact for counterparty not found.");
        }

        return (String) data.getFirst().get("Ref");
    }

    public String createCounterparty(CreateCounterpartyRequest createCounterpartyRequest) {
        Map<String, Object> methodProperties = Map.of(
                "FirstName", createCounterpartyRequest.FirstName(),
                "MiddleName", createCounterpartyRequest.MiddleName(),
                "LastName", createCounterpartyRequest.LastName(),
                "Phone", createCounterpartyRequest.Phone(),
                "Email", createCounterpartyRequest.Email(),
                "CounterpartyType", "PrivatePerson",
                "CounterpartyProperty", "Recipient"
        );

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "CounterpartyGeneral",
                "save",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        List<Map<String, Object>> data = response.data();

        if (data == null || data.isEmpty()) {
            throw new IllegalStateException("Failed to create counterparty.");
        }

        return (String) data.getFirst().get("Ref");
    }

    public String getCounterpartyIfExists(String fullName) {
        Map<String, Object> methodProperties = Map.of(
                "FindByString", fullName,
                "CounterpartyType", "PrivatePerson",
                "CounterpartyProperty", "Recipient"
        );

        NovaPoshtaRequest request = new NovaPoshtaRequest(
                properties.apiKey(),
                "Counterparty",
                "getCounterparties",
                methodProperties
        );

        NovaPoshtaResponse response = novaPoshtaClient.post(request);
        List<Map<String, Object>> data = response.data();

        if (data == null || data.isEmpty()) {
            return null;
        }

        return (String) data.getFirst().get("Ref");
    }

    private String resolveCounterparty(
            String firstName,
            String middleName,
            String lastName,
            String phone,
            String email
    ) {
        String fullName = String.join(" ", firstName, middleName, lastName).trim();

        String existingCounterpartyRef = getCounterpartyIfExists(fullName);
        if (existingCounterpartyRef != null) {
            return existingCounterpartyRef;
        }

        CreateCounterpartyRequest createRequest = new CreateCounterpartyRequest(
                firstName,
                middleName,
                lastName,
                phone,
                email
        );

        return createCounterparty(createRequest);
    }

    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("CostOnSite must be a number");
    }
}
