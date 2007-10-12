
    create table Billing_Reports (
        Report_Id bigint not null,
        From_Time timestamp not null,
        Recieved_Time timestamp not null,
        To_Time timestamp not null,
        Appliance_Id bigint not null,
        primary key (Report_Id)
    );

    create table Billings_Report_Details (
        Report_Detail_Id bigint not null,
        Database varchar(128) not null,
        EADO_Type varchar(128) not null,
        Managed_Bytes bigint not null,
        Total_Bytes bigint not null,
        Report_Id bigint not null,
        primary key (Report_Detail_Id)
    );

    create table Billings_Report_Headers (
        Report_Header_Id bigint not null,
        EADO_Count bigint not null,
        EADO_Count_Recent bigint not null,
        EADO_Type varchar(128) not null,
        Report_Id bigint not null,
        primary key (Report_Header_Id)
    );

    create table Customer_Appliances (
        Appliance_Id bigint not null,
        Name varchar(250) not null,
        Serial_Number varchar(250) not null,
        Customer_Id bigint not null,
        primary key (Appliance_Id)
    );

    create table Customer_Billings (
        Licence_Id bigint not null,
        Appliance_Id bigint not null,
        primary key (Licence_Id)
    );

    create table Customer_Licenses (
        Licence_Id bigint not null,
        Appliance_Id bigint not null,
        primary key (Licence_Id)
    );

    create table Customer_Products (
        Customer_Product_Id bigint not null,
        Licence_Id bigint not null,
        Product_Id bigint not null,
        primary key (Customer_Product_Id)
    );

    create table Licenses (
        Licence_Id bigint not null,
        Create_Time timestamp not null,
        Licence_Key blob(64000) not null,
        primary key (Licence_Id)
    );

    create table Mail_Config (
        Mail_Config_Id bigint not null,
        Schedule_Time bigint not null,
        Server_Address varchar(250) not null,
        Server_Port integer not null,
        User_Name varchar(128),
        User_Password varchar(128),
        primary key (Mail_Config_Id)
    );

    create table TITAN_Customers (
        Customer_Id bigint not null,
        Description varchar(1024),
        Name varchar(250) not null unique,
        primary key (Customer_Id)
    );

    create table TITAN_Products (
        Customer_Id bigint not null,
        Abbreviation varchar(120) not null,
        Description varchar(1024),
        Name varchar(250) not null unique,
        primary key (Customer_Id)
    );

    alter table Billing_Reports 
        add constraint FK8EACBFBB4950ABF1 
        foreign key (Appliance_Id) 
        references Customer_Appliances;

    alter table Billings_Report_Details 
        add constraint FKED439BDEA28929AA 
        foreign key (Report_Id) 
        references Billing_Reports;

    alter table Billings_Report_Headers 
        add constraint FKBFD21922A28929AA 
        foreign key (Report_Id) 
        references Billing_Reports;

    alter table Customer_Appliances 
        add constraint FK66DAD367EA9295C5 
        foreign key (Customer_Id) 
        references TITAN_Customers;

    alter table Customer_Billings 
        add constraint FK60E17C994950ABF1 
        foreign key (Appliance_Id) 
        references Customer_Appliances;

    alter table Customer_Billings 
        add constraint FK60E17C9911A2A5EF 
        foreign key (Licence_Id) 
        references Licenses;

    alter table Customer_Licenses 
        add constraint FK5FF153934950ABF1 
        foreign key (Appliance_Id) 
        references Customer_Appliances;

    alter table Customer_Licenses 
        add constraint FK5FF1539311A2A5EF 
        foreign key (Licence_Id) 
        references Licenses;

    alter table Customer_Products 
        add constraint FKEFFD20A5B26E392F 
        foreign key (Product_Id) 
        references TITAN_Products;

    alter table Customer_Products 
        add constraint FKEFFD20A5B7E12331 
        foreign key (Licence_Id) 
        references Customer_Licenses;
create sequence SEQ_BILLING_REPORT;
create sequence SEQ_BILLING_REPORT_DETAILS;
create sequence SEQ_BILLING_REPORT_HEADER;
create sequence SEQ_CUSTOMER;
create sequence SEQ_CUSTOMER_APPLIANCE;
create sequence SEQ_CUSTOMER_PRODUCTS;
create sequence SEQ_LICENCE;
create sequence SEQ_PRODUCT;
