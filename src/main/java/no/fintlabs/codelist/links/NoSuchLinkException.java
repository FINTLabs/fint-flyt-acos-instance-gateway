package no.fintlabs.codelist.links;

import no.fint.model.resource.FintLinks;

public class NoSuchLinkException extends RuntimeException {

    public static NoSuchLinkException noSelfLink(FintLinks resource) {
        return new NoSuchLinkException(String.format("No self link in resource"));
    }

    public static NoSuchLinkException noLink(FintLinks resource, String linkedResourceName) {
        return new NoSuchLinkException(String.format("No link for resource"));
    }

    public NoSuchLinkException(String message) {
        super(message);
    }

}
