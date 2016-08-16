package resource;


import common.Page;
import common.SearcherConstants;
import org.apache.lucene.queryparser.classic.ParseException;
import service.IndexReaderService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/api")
public class SearchAPI {

    public IndexReaderService indexReaderService;

    public SearchAPI() throws IOException {
        indexReaderService = new IndexReaderService(SearcherConstants.INDEXING_PATH);
    }

    @GET
    @Path("/search/{searchQuery}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPages(@PathParam("searchQuery") String searchQuery) throws IOException, ParseException {
        List<Page> pages = indexReaderService.queryIndex(searchQuery);
        GenericEntity<List<Page>> entity = new GenericEntity<List<Page>>(pages) {};
        return Response.ok(entity).build();
    }
}
